package sanity.nil.meta.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import sanity.nil.meta.model.FileJournalModel;

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
}
