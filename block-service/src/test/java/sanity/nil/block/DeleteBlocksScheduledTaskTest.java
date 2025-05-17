package sanity.nil.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.PutObjectArgs;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.model.BlockModel;
import sanity.nil.block.model.TaskModel;
import sanity.nil.exceptions.StorageException;
import sanity.nil.minio.MinioOperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(QuarkusIntegrationTestResources.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class DeleteBlocksScheduledTaskTest {

    @Inject
    EntityManager entityManager;
    @Inject
    MinioOperations minioOperations;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    UserTransaction userTransaction;

    @Test
    public void given_Blocks_For_Two_Files_When_Delete_Blocks_Task_Created_Minio_Objects_Removed_And_Database_Entries_Deleted() throws Exception {
        File directory1 = getDirectory("files");
        File directory2 = copyResourceDirectory(directory1, "files-copy");

        userTransaction.begin();
        List<String> savedFiles1 = uploadFilesFromDirectoryToMinio(directory1);
        List<String> savedFiles2 = uploadFilesFromDirectoryToMinio(directory2);
        var task1 = saveTask(savedFiles1);
        var task2 = saveTask(savedFiles2);
        userTransaction.commit();

        assertBlocksDeletedTaskFinished(savedFiles1, task1);
        assertBlocksDeletedTaskFinished(savedFiles2, task2);
    }

    private void assertBlocksDeletedTaskFinished(List<String> savedFiles, TaskModel task) {
        Awaitility
                .await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> assertBlocksDeleted(savedFiles), Matchers.equalTo(true));

        var executedTask = entityManager.createQuery("SELECT t FROM TaskModel t WHERE t.id = :id", TaskModel.class)
                .setParameter("id", task.getId())
                .getSingleResult();
        Assertions.assertEquals(TaskStatus.FINISHED, executedTask.getStatus());
    }

    // returns true if both records in db for all files have been deleted and minio objects
    private boolean assertBlocksDeleted(List<String> hashList) throws Exception {
        userTransaction.begin();
        long count = entityManager.createQuery(
                        "SELECT count(b) FROM BlockModel b WHERE b.hash IN :hashes", Long.class)
                .setParameter("hashes", hashList)
                .getSingleResult();
        userTransaction.commit();

        if (count > 0) {
            return false;
        }

        for (String hash : hashList) {
            try {
                minioOperations.statObject(hash);
                return false;
            } catch (StorageException e) {
                if (!e.getMessage().startsWith("NoSuchKey")) {
                    throw e;
                }
                log.info("Object successfully deleted: " + hash);
            }
        }

        return true;
    }

    private TaskModel saveTask(List<String> filesToDelete) {
        var metadata = objectMapper.createObjectNode();
        metadata.putPOJO("blocksToDelete", filesToDelete);
        var delayedTask = new TaskModel(null, TaskType.DELETE_BLOCKS, metadata, TaskStatus.CREATED);
        entityManager.persist(delayedTask);
        return delayedTask;
    }

    private List<String> uploadFilesFromDirectoryToMinio(File directory) throws Exception {
        List<String> savedFiles = new ArrayList<>();
        for (File file : directory.listFiles()) {
            var filename = StringUtils.substringBeforeLast(file.getName(), ".");
            entityManager.persist(new BlockModel(filename));
            try (InputStream inputStream = new FileInputStream(file)) {
                minioOperations.putObject(
                        PutObjectArgs.builder()
                                .object(filename)
                                .stream(inputStream, file.length(), -1)
                                .contentType("application/octet-stream")
                );
                log.info("Saved file " + filename);
                savedFiles.add(filename);
            }
        }
        return savedFiles;
    }

    private File getDirectory(String directoryName) throws URISyntaxException {
        URL resourceUrl = getClass().getClassLoader().getResource(directoryName);
        if (resourceUrl == null) {
            throw new IllegalStateException("Resource directory " + directoryName + " not found");
        }
        Path resourcePath = Paths.get(resourceUrl.toURI());
        return resourcePath.toFile();
    }

    private File copyResourceDirectory(File sourceDir, String targetDir) throws Exception {
        File parentDir = sourceDir.getParentFile();
        File targetDirectory = new File(parentDir, targetDir);
        Path targetPath = targetDirectory.toPath();
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        Path sourcePath = sourceDir.toPath();
        Files.walk(sourcePath).forEach(source -> {
            try {
                if (source.toFile().isDirectory()) return;
                Path relativePath = sourcePath.relativize(source);
                String fileName = relativePath.getFileName().toString();
                int dotIndex = fileName.lastIndexOf('.');
                String newFileName = (dotIndex != -1)
                        ? fileName.substring(0, dotIndex) + "-test" + fileName.substring(dotIndex)
                        : fileName + "-test";

                Path destinationDir = targetPath.resolve(relativePath.getParent() != null
                        ? relativePath.getParent()
                        : Path.of(""));

                log.info("Copying file " + source.getFileName().toString() + " to " + destinationDir.getFileName().toString());
                Path destination = destinationDir.resolve(newFileName);
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

            } catch (IOException e) {
                throw new RuntimeException("Failed to copy resource", e);
            }
        });
        return targetDirectory;
    }

}
