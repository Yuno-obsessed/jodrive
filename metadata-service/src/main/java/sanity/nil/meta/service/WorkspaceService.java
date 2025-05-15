package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.file.CreateDirectory;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.UserWorkspaceModel;
import sanity.nil.meta.model.WorkspaceModel;
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

    public WorkspaceDTO getWorkspace(String id) {
        var workspaceID = Long.valueOf(id);
        var workspaces = entityManager.createQuery("SELECT m FROM WorkspaceModel m " +
                        "WHERE m.id = ?1 ", WorkspaceModel.class)
                .setParameter(1, workspaceID)
                .getResultList();
        if (workspaces == null || workspaces.isEmpty())
            throw new NoSuchElementException();

        var workspace = workspaces.getFirst();

        return new WorkspaceDTO(workspace.getName(), workspace.getDescription());
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
                                "WHERE u.id.workspaceID = :workspaceID " +
                                "GROUP BY u.id.userID, u.role, u.user.id, u.user.username",
                        UserWorkspaceModel.class)
                .setParameter("workspaceID", workspaceID)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        List<WorkspaceUserDTO> dtos = users.stream()
                .map(u -> new WorkspaceUserDTO(
                        u.getUser().getUsername(),
                        null,
                        u.getRole()
                )).toList();

        int totalPages = (int) Math.ceil((double) totalUsers / size);
        boolean hasNext = (page + 1) < totalPages;
        boolean hasPrevious = page > 0;

        return new Paged<WorkspaceUserDTO>().of(dtos, totalPages, hasNext, hasPrevious);
    }

    public Long createWorkspace(CreateWorkspaceDTO dto) {
        var identity = identityProvider.getCheckedIdentity();
        var newWorkspace = new WorkspaceModel(dto.name(), dto.description());
        try {
            userTransaction.begin();
            entityManager.persist(newWorkspace);
            var creator = entityManager.find(UserModel.class, identity.getUserID());
            var workspaceUser = new UserWorkspaceModel(newWorkspace, creator, "OWNER");
            entityManager.persist(workspaceUser);
            // TODO: refactor this
            metadataService.createDirectory(new CreateDirectory(newWorkspace.getId(), "", ""));
            userTransaction.commit();
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException("Error creating workspace");
        }
        return newWorkspace.getId();
    }
}
