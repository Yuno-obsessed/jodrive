package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.util.CollectionUtils;

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
        journal.setNewID(journal.getWorkspace().getId(), nextId);
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
}
