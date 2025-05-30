package sanity.nil.block.presentation.scheduled;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.minio.RemoveObjectsArgs;
import io.minio.messages.DeleteObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.model.TaskModel;
import sanity.nil.minio.MinioOperations;
import sanity.nil.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class DeleteBlocksJob implements Job {

    @Inject
    UserTransaction userTransaction;
    @Inject
    MinioOperations minioOperations;
    @Inject
    EntityManager entityManager;
    // TODO: add deletes in batches, because one file can have hundreds-thousands of blocks
    int pageSize = 100;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Job DELETE_BLOCKS is running");
        try {
            userTransaction.begin();
            var tasks = entityManager.createQuery("SELECT t FROM TaskModel t WHERE " +
                            "t.action = :action AND (t.status IN :normalStatus " +
                            "OR (t.status IN :retry AND t.retries < :retryThreshold)) " +
                            "ORDER BY t.updatedAt ", TaskModel.class)
                    .setParameter("action", TaskType.DELETE_BLOCKS)
                    .setParameter("normalStatus", TaskStatus.CREATED)
                    .setParameter("retry", TaskStatus.IN_RETRY)
                    .setParameter("retryThreshold", 3)
                    .setMaxResults(1)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();
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

    public void performTask(TaskModel task) throws Exception {
        userTransaction.begin();
        try {
            var metadata = task.getMetadata();
            var blockToDeleteMeta = metadata.get("blocksToDelete");
            List<String> blocksToDelete = Arrays.stream(blockToDeleteMeta.asText().split(","))
                    .collect(Collectors.toCollection(ArrayList::new));
            int i = 0;
            log.info("Blocks to delete: " + blocksToDelete.size());
            deleteBlocks(blocksToDelete);
            log.info("Job DELETE_BLOCKS for task " + task.getId() + " is completed");
        } catch (Exception e) {
            userTransaction.rollback();
            log.info("Error deleting blocks for object " + task.getObjectID(), e);

            userTransaction.begin();
            task.setStatus(TaskStatus.FAILED);
            entityManager.merge(task);
            userTransaction.commit();
        }
        task.setStatus(TaskStatus.FINISHED);
        entityManager.merge(task);
        userTransaction.commit();
    }

    public void deleteBlocks(List<String> blockHashes) throws Exception {
        try {
            int deletes = entityManager.createQuery("DELETE FROM BlockModel b WHERE b.hash IN :hashes")
                    .setParameter("hashes", blockHashes)
                    .executeUpdate();
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
