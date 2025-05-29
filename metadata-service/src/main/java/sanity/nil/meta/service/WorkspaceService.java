package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.file.CreateDirectory;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.model.*;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.IdentityProvider;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

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
            var subscriptionID = creator.getSubscription().getId();
            var statistics = entityManager.createQuery("SELECT s FROM UserStatisticsModel s " +
                            "WHERE s.id.userID = :userID AND s.id.statisticsID = :statisticsID", UserStatisticsModel.class)
                    .setParameter("userID", identity.getUserID())
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
            if (workspacesUserIn - workspacesLimit < 1) {
                throw new InsufficientQuotaException(Quota.USER_WORKSPACES, workspacesLimit.toString());
            }
            entityManager.persist(newWorkspace);
            var workspaceUser = new UserWorkspaceModel(newWorkspace, creator, "OWNER");
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
}
