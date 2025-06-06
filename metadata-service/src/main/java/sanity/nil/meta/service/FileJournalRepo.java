package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.StringUtils;
import sanity.nil.meta.consts.Constants;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.dto.file.FileFilters;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FileJournalRepo {

    @Inject
    EntityManager entityManager;

    public FileJournalModel insert(FileJournalModel journal) {
        Long nextId = (Long) entityManager
                .createNativeQuery("SELECT metadata_db.next_file_id(?1)")
                .setParameter(1, journal.getWorkspace().getId())
                .getSingleResult();
        Integer historyID = entityManager.createQuery("SELECT COALESCE(max(historyID),0) FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID", Integer.class)
                .setParameter("wsID", journal.getId().getWorkspaceID())
                .getSingleResult();
        journal.setNewID(journal.getWorkspace().getId(), nextId);
        journal.setHistoryID(++historyID);
        entityManager.persist(journal);
        return journal;
    }

    public Optional<FileJournalModel> findById(Long fileID, Long wsID) {
        var res = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                "WHERE id.fileID = :fileID AND id.workspaceID = :wsID", FileJournalModel.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", wsID)
                .getResultList();
        return CollectionUtils.isEmpty(res) ? Optional.empty() : Optional.of(res.getFirst());
    }

    public Optional<FileJournalModel> findLatestByPathAndState(Long wsID, String path, FileState fileState) {
        var res = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE id.workspaceID = :wsID AND f.path = :path " +
                        "AND f.state = :state " +
                        "AND f.latest = 1 " +
                        "ORDER BY f.historyID DESC", FileJournalModel.class)
                .setParameter("wsID", wsID)
                .setParameter("path", path)
                .setParameter("state", fileState)
                .getResultList();
        return CollectionUtils.isEmpty(res) ? Optional.empty() : Optional.of(res.getFirst());
    }

    public FileJournalModel findByIdAndStateIn(Long fileID, Long wsID, FileState state) {
        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE id.fileID = :fileID AND id.workspaceID = :wsID " +
                        "AND state in :state", FileJournalModel.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", wsID)
                .setParameter("state", state)
                .getSingleResult();
    }

    public FileJournalModel findByPathAndVersion(Long wsID, String path, int version) {
        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE id.workspaceID = :wsID AND f.path = :path " +
                        "AND state in :state " +
                        "ORDER BY f.historyID ASC", FileJournalModel.class)
                .setParameter("wsID", wsID)
                .setParameter("path", path)
                .setParameter("state", FileState.UPLOADED)
                .setFirstResult(version-1)
                .setMaxResults(1)
                .getSingleResult();
    }

    public String findPathByID(Long wsID, Long id) {
        return entityManager.createQuery("SELECT f.path FROM FileJournalModel f " +
                        "WHERE id.workspaceID = :wsID AND f.id.fileID = :id " +
                        "AND state in :state " +
                        "ORDER BY f.historyID DESC", String.class)
                .setParameter("wsID", wsID)
                .setParameter("id", id)
                .setParameter("state", FileState.UPLOADED)
                .getSingleResult();
    }

    public List<Long> getVersionsByPath(Long wsID, String path) {
        return entityManager.createQuery("SELECT f.fileID FROM FileJournalModel f " +
                        "WHERE id.workspaceID = :wsID AND f.path = :path " +
                        "AND state in :state " +
                        "ORDER BY f.historyID ASC", Long.class)
                .setParameter("path", path)
                .setParameter("wsID", wsID)
                .setParameter("state", FileState.UPLOADED)
                .getResultList();
    }

    public void updateStateAndNameByPath(String updatedName, FileState updatedState, Long wsID, String path) {
        var builder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<FileJournalModel> update = builder.createCriteriaUpdate(FileJournalModel.class);
        Root<FileJournalModel> root = update.from(FileJournalModel.class);

        boolean needsUpdate = false;

        if (updatedName != null) {
            update.set(root.get("path"), updatedName);
            needsUpdate = true;
        }
        if (updatedState != null) {
            update.set(root.get("state"), updatedState);
            needsUpdate = true;
        }

        if (needsUpdate) {
            update.where(builder.equal(root.get("path"), path),
                    builder.equal(root.get("id").get("workspaceID"), wsID)
            );
            entityManager.createQuery(update).executeUpdate();
        }
    }

    public List<Predicate> buildPredicatesFromParams(CriteriaBuilder cb, Root<FileJournalModel> root, FileFilters filters) {
        List<Predicate> predicates = new ArrayList<>();
        if (filters.wsID() != null) {
            predicates.add(cb.equal(root.get("id").get("workspaceID"), filters.wsID()));
        }
        if (StringUtils.isNotEmpty(filters.name())) {
            predicates.add(cb.like(cb.upper(root.get("path")), "%" + filters.name().toUpperCase() + "%"));
        }
        // exclude root dirs
        predicates.add(cb.notEqual(root.get("path"), "/"));
        if (filters.deleted() != null && filters.deleted()) {
            predicates.add(cb.equal(root.get("state"), FileState.DELETED));
        } else {
            predicates.add(cb.equal(root.get("state"), FileState.UPLOADED));
        }
        if (filters.userID() != null) {
            predicates.add(cb.equal(root.get("uploader").get("id"), filters.userID()));
        }
        predicates.add(cb.equal(root.get("latest"), (short) 1));
        return predicates;
    }

    public List<Predicate> buildPredicatesFromParams(CriteriaBuilder cb, Root<FileJournalModel> root, Long wsID, String pathLike) {
        List<Predicate> predicates = new ArrayList<>();
        if (!pathLike.endsWith("/")) {
            pathLike += "/";
        } else {
            pathLike += "_";
        }

        Expression<String> pathExpr = cb.upper(root.get("path"));
        String upperPathLike = pathLike.toUpperCase();

        predicates.add(cb.equal(root.get("id").get("workspaceID"), wsID));
        predicates.add(cb.like(pathExpr, upperPathLike + "%"));

        Expression<String> subPath = cb.function(
                "SUBSTRING",
                String.class,
                pathExpr,
                cb.literal(upperPathLike.length() + 1)
        );

        predicates.add(cb.notLike(subPath, "%/_%"));
        predicates.add(cb.equal(root.get("latest"), (short) 1));

        predicates.add(cb.equal(root.get("state"), FileState.UPLOADED));
        return predicates;
    }

    public List<FileInfo> getFileNodesByFilters(Long wsID, String path) {
        if (path.endsWith(Constants.DIRECTORY_CHAR.toString())) {
            path = path + "%";
        } else {
            path = null;
        }
        return entityManager.createQuery("SELECT new sanity.nil.meta.dto.file.FileInfo(" +
                        "f.fileID, f.id.workspaceID, f.path, false, f.size, f.uploader.id, " +
                        "f.uploader.username, f.createdAt) " +
                        "FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND (:path IS NULL OR f.path LIKE :path) " +
                        "AND f.state = :state ", FileInfo.class)
                .setParameter("wsID", wsID)
                .setParameter("path", path)
                .setParameter("state", FileState.UPLOADED)
                .getResultList();
    }
}
