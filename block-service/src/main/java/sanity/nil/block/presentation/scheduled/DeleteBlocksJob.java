package sanity.nil.block.presentation.scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.jooq.DSLContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.db.tables.records.TasksRecord;
import sanity.nil.minio.MinioOperations;
import sanity.nil.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sanity.nil.block.db.tables.Blocks.BLOCKS;
import static sanity.nil.block.db.tables.Tasks.TASKS;

@JBossLog
@ApplicationScoped
public class DeleteBlocksJob implements Job {

    @Inject
    UserTransaction userTransaction;
    @Inject
    MinioOperations minioOperations;
    @Inject
    DSLContext dslContext;
    @Inject
    ObjectMapper objectMapper;
    // TODO: add deletes in batches, because one file can have hundreds-thousands of blocks
    int pageSize = 100;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Job DELETE_BLOCKS is running");
        try {
            userTransaction.begin();
            var tasks = dslContext.selectFrom(TASKS)
                    .where(TASKS.ACTION.eq(TaskType.DELETE_BLOCKS.name())
                            .and(TASKS.STATUS.in(TaskStatus.CREATED.name())
                                    .or(TASKS.STATUS.in(TaskStatus.IN_RETRY.name())
                                            .and(TASKS.RETRIES.lt((short) 3))
                                    )))
                    .orderBy(TASKS.UPDATED_AT.asc()).limit(1).forUpdate()
                    .fetch();
            userTransaction.commit();
            if (CollectionUtils.isNotEmpty(tasks)) {
                log.info("Job DELETE_BLOCKS is deleting " + tasks.size() + " tasks");
                for (var task : tasks) {
                    performTask(task);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void performTask(TasksRecord task) throws Exception {
        userTransaction.begin();
        try {
            var metadata = objectMapper.readTree(task.getMetadata().data());
            var blockToDeleteMeta = metadata.get("blocksToDelete");
            List<String> blocksToDelete = Arrays.stream(blockToDeleteMeta.asText().split(","))
                    .collect(Collectors.toCollection(ArrayList::new));
            int i = 0;
            log.info("Blocks to delete: " + blocksToDelete.size());
            deleteBlocks(blocksToDelete);
            log.info("Job DELETE_BLOCKS for task " + task.getId() + " is completed");
        } catch (Exception e) {
            userTransaction.rollback();
            log.info("Error deleting blocks for object " + task.getObjectId(), e);

            userTransaction.begin();
            task.setStatus(TaskStatus.FAILED.name());
            task.update(TASKS.STATUS);
            userTransaction.commit();
        }
        task.setStatus(TaskStatus.FINISHED.name());
        task.update(TASKS.STATUS);
        userTransaction.commit();
    }

    public void deleteBlocks(List<String> blockHashes) throws Exception {
        try {
            int deletes = dslContext.delete(BLOCKS).where(BLOCKS.HASH.in(blockHashes))
                    .execute();
            var minioDeleteObject = blockHashes.stream().map(DeleteObject::new).toList();
            int minioRes = minioOperations.removeObjects(
                    RemoveObjectsArgs.builder()
                            .objects(minioDeleteObject)
                            .bypassGovernanceMode(true)
            );
            log.info("Deleted " + minioRes + " s3 objects");
            log.info("Deleted " + deletes + " rows");
        } catch (Exception e) {
            throw new RuntimeException("Error deleting minio files", e);
        }
    }
}
