package sanity.nil.meta.service;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.consts.WsRole;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.file.CreateDirectory;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.model.*;
import sanity.nil.meta.security.CryptoManager;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.IdentityProvider;

import java.util.*;

@JBossLog
@ApplicationScoped
public class WorkspaceService {

    @Inject
    EntityManager entityManager;
    @Inject
    @Named("keycloakIdentityProvider")
    IdentityProvider identityProvider;
    @Inject
    UserTransaction userTransaction;
    @Inject
    MetadataService metadataService;
    @Inject
    SubscriptionQuotaCache subscriptionQuotaCache;
    @Inject
    MinioOperations minioOperations;
    @Inject
    LinkEncoder linkEncoder;
    private final String AVATAR_BUCKET = "user.avatars";

    public WorkspaceDTO getWorkspace(String id) {
        var workspaceID = Long.valueOf(id);
        var workspaces = entityManager.createQuery("SELECT m FROM WorkspaceModel m " +
                        "WHERE m.id = ?1 ", WorkspaceModel.class)
                .setParameter(1, workspaceID)
                .getResultList();
        if (workspaces == null || workspaces.isEmpty())
            throw new NoSuchElementException();

        var workspace = workspaces.getFirst();

        return new WorkspaceDTO(workspace.getId(), workspace.getName(), workspace.getDescription());
    }

    public List<WorkspaceDTO> getUserWorkspaces() {
        var identity = identityProvider.getCheckedIdentity();
        var workspaces = entityManager.createQuery("SELECT um.workspace FROM UserWorkspaceModel um " +
                        "WHERE um.user.id = :userID ", WorkspaceModel.class)
                .setParameter("userID", identity.getUserID())
                .getResultList();

        return workspaces.stream().map(w -> new WorkspaceDTO(w.getId(), w.getName(), w.getDescription())).toList();
    }

    public Paged<WorkspaceUserDTO> getWorkspaceUsers(String id, int page, int size) {
        var workspaceID = Long.valueOf(id);
        Long totalUsers = entityManager.createQuery("SELECT COUNT(DISTINCT u.id.userID) FROM UserWorkspaceModel u " +
                        "WHERE u.id.workspaceID = :workspaceID", Long.class)
                .setParameter("workspaceID", workspaceID)
                .getSingleResult();

        if (totalUsers == 0)
            return new Paged<WorkspaceUserDTO>().of(Collections.emptyList(), 0, false, false);

        List<UserWorkspaceModel> users = entityManager.createQuery(
                        "SELECT u FROM UserWorkspaceModel u " +
                                "JOIN FETCH u.user " +
                                "WHERE u.id.workspaceID = :workspaceID ",
                        UserWorkspaceModel.class)
                .setParameter("workspaceID", workspaceID)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        List<WorkspaceUserDTO> dtos = users.stream()
                .map(u -> new WorkspaceUserDTO(
                        u.getUser().getId(),
                        u.getUser().getUsername(),
                        u.getUser().getAvatar() == null ? null :
                                minioOperations.getObjectURL(AVATAR_BUCKET, u.getUser().getAvatar()),
                        u.getRole(),
                        u.getJoinedAt()
                )).toList();

        int totalPages = (int) Math.ceil((double) totalUsers / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<WorkspaceUserDTO>().of(dtos, totalPages, hasNext, hasPrevious);
    }

    public WorkspaceDTO createWorkspace(CreateWorkspaceDTO dto) {
        var identity = identityProvider.getCheckedIdentity();
        var newWorkspace = new WorkspaceModel(dto.name(), dto.description());
        try {
            userTransaction.begin();
            var creator = entityManager.find(UserModel.class, identity.getUserID());
            verifyUserQuota(identity.getUserID(), creator.getSubscription().getId());
            entityManager.persist(newWorkspace);
            var workspaceUser = new UserWorkspaceModel(newWorkspace, creator, WsRole.OWNER);
            entityManager.persist(workspaceUser);
            // TODO: refactor this
            metadataService.createDirectory(new CreateDirectory(newWorkspace.getId(), "", ""));
            userTransaction.commit();
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException("Error creating workspace");
        }
        return new WorkspaceDTO(newWorkspace.getId(), newWorkspace.getName(), newWorkspace.getDescription());
    }

    @Transactional
    public void kickWorkspaceUser(Long wsID, UUID userID) {
        var identity = identityProvider.getCheckedIdentity();
        var issuerRole = getUserWorkspaceRole(wsID, identity.getUserID()).get();
        var userToKickRole = getUserWorkspaceRole(wsID, userID).get();

        if (!userToKickRole.equals(WsRole.OWNER) && issuerRole.equals(WsRole.OWNER)) {
            entityManager.createQuery("DELETE FROM UserWorkspaceModel u " +
                            "WHERE u.id.userID = :userID AND u.id.workspaceID = :wsID")
                    .setParameter("userID", userID)
                    .setParameter("wsID", wsID)
                    .executeUpdate();
        } else {
            throw new ForbiddenException("Can't kick user");
        }
    }

    private Optional<WsRole> getUserWorkspaceRole(Long wsID, UUID userID) {
        try {
            return Optional.of(entityManager.createQuery("SELECT u.role FROM UserWorkspaceModel u " +
                            "WHERE u.id.workspaceID = :wsID AND u.id.userID = :userID", WsRole.class)
                    .setParameter("wsID", wsID)
                    .setParameter("userID", userID)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public String generateJoinWorkspaceLink(Long wsID) {
        var identity = identityProvider.getIdentity();
        String link = String.format("%s_join_workspace_%s", wsID, identity.getUserID());
        try {
            return linkEncoder.encrypt(link);
        } catch (CryptoException e) {
            log.error("Could not encrypt link " + link, e);
            return "";
        }
    }

    @Transactional
    public WorkspaceDTO joinWorkspace(String link) {
        var identity = identityProvider.getIdentity();
        try {
            var bareLink = linkEncoder.decrypt(link);
            var linkWs = Long.valueOf(StringUtils.substringBefore(bareLink, "_"));
            var issuerID = StringUtils.substringAfterLast(bareLink, "_");

            var userExistsInWs = getUserWorkspaceRole(linkWs, identity.getUserID());
            if (userExistsInWs.isPresent()) {
                throw new ForbiddenException("User is already in workspace");
            }

            var issuer = getUserWorkspaceRole(linkWs, UUID.fromString(issuerID));
            if (issuer.isEmpty()) {
                throw new IllegalArgumentException("Issuer is not found in workspace");
            }
            var workspace = entityManager.find(WorkspaceModel.class, linkWs);
            var newWsUser = new UserWorkspaceModel(workspace, entityManager.find(UserModel.class, identity.getUserID()), WsRole.USER);
            entityManager.persist(newWsUser);
            return new WorkspaceDTO(workspace.getId(), workspace.getName(), workspace.getDescription());
        } catch (CryptoException e) {
            log.error("Could not decrypt link " + link, e);
            throw new RuntimeException(e);
        }
    }

    private void verifyUserQuota(UUID userID, Short subscriptionID) {
        var statistics = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                        "WHERE s.id.userID = :userID AND s.id.statisticsID = :statisticsID", UserStatisticsModel.class)
                .setParameter("userID", userID)
                .setParameter("statisticsID", Quota.USER_WORKSPACES.id())
                .getSingleResult();

        var workspacesUserIn = Long.valueOf(statistics.getValue());
        var quota = subscriptionQuotaCache.getByID(subscriptionID);
        Integer workspacesLimit;
        if (quota != null) {
            workspacesLimit = quota.workspacesLimit();
        } else {
            var subscription = entityManager.find(UserSubscriptionModel.class, subscriptionID);
            workspacesLimit = subscription.getWorkspacesLimit();
        }
        if (workspacesLimit - workspacesUserIn < 1) {
            throw new InsufficientQuotaException(Quota.USER_WORKSPACES, workspacesLimit.toString());
        }
    }
}
