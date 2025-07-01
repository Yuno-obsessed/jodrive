package sanity.nil.meta.presentation.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.DSLContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sanity.nil.meta.consts.TaskStatus;
import sanity.nil.meta.consts.TaskType;
import sanity.nil.meta.db.tables.records.TasksRecord;
import sanity.nil.meta.service.MetadataService;
import sanity.nil.util.CollectionUtils;

import java.time.OffsetDateTime;

import static sanity.nil.meta.consts.Constants.FILE_VERSIONS_MAX;
import static sanity.nil.meta.db.tables.Tasks.TASKS;

@JBossLog
@ApplicationScoped
public class DeleteFileJob implements Job {

    @Inject
    UserTransaction userTransaction;
    @Inject
    DSLContext dslContext;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    MetadataService metadataService;

    @ConfigProperty(name = "application.scheduler.delete-file.pagination", defaultValue = "5")
    int pagination;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Job DELETE_FILE is running");
        try {
            userTransaction.begin();
            var tasks = dslContext.selectFrom(TASKS)
                    .where(TASKS.ACTION.eq(TaskType.DELETE_FILE.name()))
                    .and(TASKS.STATUS.notEqual(TaskStatus.FINISHED.name()))
                    .and(TASKS.PERFORM_AT.lessOrEqual(OffsetDateTime.now()))
                    .orderBy(TASKS.UPDATED_AT.desc())
                    .fetch().into(TasksRecord.class);
            userTransaction.commit();

            if (CollectionUtils.isNotEmpty(tasks)) {
                log.info("Deleting " + tasks.size() + " files");
                for (var task : tasks) {
                    performTask(task);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void performTask(TasksRecord task) throws Exception {
        userTransaction.begin();
        try {
            var metadata = objectMapper.readTree(task.getMetadata().data());
            var wsID = metadata.get("workspace").asText();
            var fileID = task.getObjectId();
            var blockList = metadataService.getFileBlockList(Long.valueOf(fileID), Long.valueOf(wsID), FILE_VERSIONS_MAX);
            metadataService.deleteFileJournal(Long.valueOf(fileID), Long.valueOf(wsID));
            metadataService.deleteBlocksWithRetry(blockList);
        } catch (Exception e) {
            userTransaction.rollback();
            log.info("Error deleting file " + task.getObjectId(), e);

            userTransaction.begin();
            task.setStatus(TaskStatus.FAILED.name());
            task.store();
            userTransaction.commit();
            return;
        }
        task.setStatus(TaskStatus.FINISHED.name());
        task.store();
        userTransaction.commit();
    }
}
