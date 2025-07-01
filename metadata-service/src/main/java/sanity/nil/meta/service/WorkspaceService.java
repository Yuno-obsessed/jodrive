package sanity.nil.meta.service;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import sanity.nil.meta.cache.SubscriptionQuotaCache;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.consts.WsRole;
import sanity.nil.meta.db.tables.records.UserStatisticsRecord;
import sanity.nil.meta.db.tables.records.UserSubscriptionsRecord;
import sanity.nil.meta.db.tables.records.UserWorkspacesRecord;
import sanity.nil.meta.db.tables.records.WorkspacesRecord;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.file.CreateDirectory;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.exceptions.CryptoException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.minio.MinioOperations;
import sanity.nil.security.IdentityProvider;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static sanity.nil.meta.db.tables.UserStatistics.USER_STATISTICS;
import static sanity.nil.meta.db.tables.UserSubscriptions.USER_SUBSCRIPTIONS;
import static sanity.nil.meta.db.tables.UserWorkspaces.USER_WORKSPACES;
import static sanity.nil.meta.db.tables.Users.USERS;
import static sanity.nil.meta.db.tables.Workspaces.WORKSPACES;

@JBossLog
@ApplicationScoped
public class WorkspaceService {

    @Inject
    DSLContext dslContext;
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
        var workspace = dslContext.selectFrom(WORKSPACES)
                .where(WORKSPACES.ID.eq(workspaceID)).fetchOne();

        return new WorkspaceDTO(workspace.getId(), workspace.getName(), workspace.getDescription());
    }

    public List<WorkspaceDTO> getUserWorkspaces() {
        var identity = identityProvider.getCheckedIdentity();
        var workspaces = dslContext
                .select(WORKSPACES.asterisk())
                .from(USER_WORKSPACES)
                .join(WORKSPACES).on(USER_WORKSPACES.WS_ID.eq(WORKSPACES.ID))
                .where(USER_WORKSPACES.USER_ID.eq(identity.getUserID()))
                .fetchInto(WorkspacesRecord.class);

        return workspaces.stream().map(w -> new WorkspaceDTO(w.getId(), w.getName(), w.getDescription())).toList();
    }

    public Paged<WorkspaceUserDTO> getWorkspaceUsers(String id, int page, int size) {
        var workspaceID = Long.valueOf(id);
        var totalUsers = dslContext.fetchCount(USER_WORKSPACES, USER_WORKSPACES.WS_ID.eq(workspaceID));

        if (totalUsers == 0)
            return new Paged<WorkspaceUserDTO>().of(Collections.emptyList(), 0, false, false);

        List<WorkspaceUserDTO> dtos = dslContext
                .select(USER_WORKSPACES.asterisk(), USERS.asterisk())
                .from(USER_WORKSPACES)
                .join(USERS).on(USER_WORKSPACES.USER_ID.eq(USERS.ID))
                .where(USER_WORKSPACES.WS_ID.eq(workspaceID))
                .offset(page * size)
                .limit(size)
                .fetch()
                .map(record -> new WorkspaceUserDTO(
                        record.get(USERS.ID),
                        record.get(USERS.USERNAME),
                        record.get(USERS.AVATAR) == null ? null :
                                minioOperations.getObjectURL(AVATAR_BUCKET, record.get(USERS.AVATAR)),
                        WsRole.valueOf(record.get(USER_WORKSPACES.ROLE)),
                        record.get(USER_WORKSPACES.JOINED_AT)
                ));

        int totalPages = (int) Math.ceil((double) totalUsers / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<WorkspaceUserDTO>().of(dtos, totalPages, hasNext, hasPrevious);
    }

    public WorkspaceDTO createWorkspace(CreateWorkspaceDTO dto) {
        var identity = identityProvider.getCheckedIdentity();
        var newWorkspace = new WorkspacesRecord(null, dto.name(), dto.description());
        try {
            userTransaction.begin();
            var creatorSubscription = dslContext.select(USER_SUBSCRIPTIONS.asterisk())
                    .from(USERS)
                    .join(USER_SUBSCRIPTIONS).on(USERS.SUBSCRIPTION_ID.eq(USER_SUBSCRIPTIONS.ID))
                    .where(USERS.ID.eq(identity.getUserID()))
                    .fetchOne().into(UserSubscriptionsRecord.class);
            verifyUserQuota(identity.getUserID(), creatorSubscription.getId());
            newWorkspace.store();
            var workspaceUser = new UserWorkspacesRecord(identity.getUserID(), newWorkspace.getId(),
                    WsRole.OWNER.name(), OffsetDateTime.now());
            workspaceUser.store();
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
            dslContext.deleteFrom(USER_WORKSPACES)
                    .where(USER_WORKSPACES.USER_ID.eq(userID))
                        .and(USER_WORKSPACES.WS_ID.eq(wsID))
                    .execute();
        } else {
            throw new ForbiddenException("Can't kick user");
        }
    }

    private Optional<WsRole> getUserWorkspaceRole(Long wsID, UUID userID) {
        var roleOp = dslContext.select(USER_WORKSPACES.ROLE)
                .from(USER_WORKSPACES)
                .where(USER_WORKSPACES.USER_ID.eq(userID)
                .and(USER_WORKSPACES.WS_ID.eq(wsID)))
                .fetchOptional();
        return roleOp.map(stringRecord1 -> WsRole.valueOf(stringRecord1.value1()));
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
            var workspace = dslContext.selectFrom(WORKSPACES)
                    .where(WORKSPACES.ID.eq(linkWs))
                    .fetchOne().into(WorkspacesRecord.class);
            var newWsUser = new UserWorkspacesRecord(identity.getUserID(), linkWs, WsRole.USER.name(), OffsetDateTime.now());
            newWsUser.store();
            return new WorkspaceDTO(workspace.getId(), workspace.getName(), workspace.getDescription());
        } catch (CryptoException e) {
            log.error("Could not decrypt link " + link, e);
            throw new RuntimeException(e);
        }
    }

    private void verifyUserQuota(UUID userID, Short subscriptionID) {
        var statistics = dslContext.selectFrom(USER_STATISTICS)
                .where(USER_STATISTICS.USER_ID.eq(userID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(Quota.USER_WORKSPACES.id()))
                .fetchOne().into(UserStatisticsRecord.class);

        var workspacesUserIn = Long.valueOf(statistics.getValue());
        var quota = subscriptionQuotaCache.getByID(subscriptionID);
        Integer workspacesLimit;
        if (quota != null) {
            workspacesLimit = quota.workspacesLimit();
        } else {
            var subscription = dslContext.selectFrom(USER_SUBSCRIPTIONS)
                    .where(USER_SUBSCRIPTIONS.ID.eq(subscriptionID))
                    .fetchOne().into(UserSubscriptionsRecord.class);
            workspacesLimit = subscription.getWorkspacesLimit();
        }
        if (workspacesLimit - workspacesUserIn < 1) {
            throw new InsufficientQuotaException(Quota.USER_WORKSPACES, workspacesLimit.toString());
        }
    }
}
