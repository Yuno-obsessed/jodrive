package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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

    public Optional<FileJournalModel> findByIdAndStateIn(Long fileID, Long wsID, FileState states) {
        var res = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE id.fileID = :fileID AND id.workspaceID = :wsID " +
                        "AND state in :states", FileJournalModel.class)
                .setParameter("fileID", fileID)
                .setParameter("wsID", wsID)
                .setParameter("states", states)
                .getResultList();
        return CollectionUtils.isEmpty(res) ? Optional.empty() : Optional.of(res.getFirst());
    }

    public List<Predicate> buildPredicatesFromParams(CriteriaBuilder cb, Root<FileJournalModel> root, FileFilters filters) {
        List<Predicate> predicates = new ArrayList<>();
        if (filters.wsID() != null) {
            predicates.add(cb.equal(root.get("id").get("workspaceID"), filters.wsID()));
        }
        if (StringUtils.isNotEmpty(filters.name())) {
            predicates.add(cb.like(cb.upper(root.get("path")), "%" + filters.name().toUpperCase() + "%"));
        }
        if (filters.deleted() != null && filters.deleted()) {
            predicates.add(cb.equal(root.get("state"), FileState.DELETED));
        } else {
            predicates.add(cb.equal(root.get("state"), FileState.UPLOADED));
        }
        if (filters.userID() != null) {
            predicates.add(cb.equal(root.get("uploader").get("id"), filters.userID()));
        }
        predicates.add(cb.notEqual(root.get("size"), 0L));
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
