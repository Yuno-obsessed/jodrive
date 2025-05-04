package sanity.nil.meta.presentation.scheduled;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sanity.nil.meta.consts.TaskStatus;
import sanity.nil.meta.consts.TaskType;
import sanity.nil.meta.model.TaskModel;
import sanity.nil.meta.service.MetadataService;
import sanity.nil.util.CollectionUtils;

import static sanity.nil.meta.consts.Constants.FILE_VERSIONS_MAX;

@JBossLog
@ApplicationScoped
public class DeleteFileJob implements Job {

    @Inject
    EntityManager entityManager;
    @Inject
    MetadataService metadataService;

    @ConfigProperty(name = "application.scheduler.delete-file.pagination", defaultValue = "5")
    int pagination;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Job DELETE_FILE is running");
        var tasks = entityManager.createQuery("SELECT t FROM TaskModel t " +
                        "WHERE t.action = :type AND t.status <> :status " +
                        "AND t.performAt <= CURRENT_TIMESTAMP " +
                        "ORDER BY t.updatedAt ASC " +
                        "FETCH FIRST :pagination ROWS ONLY", TaskModel.class)
                .setParameter("type", TaskType.DELETE_FILE)
                .setParameter("status", TaskStatus.FINISHED)
                .setParameter("pagination", pagination)
                .getResultList();

        if (CollectionUtils.isNotEmpty(tasks)) {
            log.info("Deleting " + tasks.size() + " files");
            tasks.forEach(task -> {
                try {
                    var metadata = task.getMetadata();
                    var wsID = metadata.get("workspace").asText();
                    var fileID = task.getObjectID();
                    var blockList = metadataService.getFileBlockList(fileID, wsID, FILE_VERSIONS_MAX);
                    metadataService.deleteFileJournal(Long.valueOf(fileID), Long.valueOf(wsID));
                    metadataService.deleteBlocksWithRetry(blockList);
                } catch (Exception e) {
                    log.info("Error deleting file " + task.getObjectID(), e);
                    return;
                }
                task.setStatus(TaskStatus.FINISHED);
                entityManager.merge(task);
            });
        }
    }
}
