package sanity.nil.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.minio.PutObjectArgs;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sanity.nil.block.consts.TaskStatus;
import sanity.nil.block.consts.TaskType;
import sanity.nil.block.db.tables.records.TasksRecord;
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

import static sanity.nil.block.db.tables.Blocks.BLOCKS;
import static sanity.nil.block.db.tables.Tasks.TASKS;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(QuarkusIntegrationTestResources.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class DeleteBlocksScheduledTaskTest {

    @Inject
    DSLContext dslContext;
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

    private void assertBlocksDeletedTaskFinished(List<String> savedFiles, TasksRecord task) {
        Awaitility
                .await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> assertBlocksDeleted(savedFiles), Matchers.equalTo(true));

        var executedTask = dslContext.selectFrom(TASKS).where(TASKS.ID.eq(task.getId()))
                .fetchOne();

        Assertions.assertEquals(TaskStatus.FINISHED.name(), executedTask.getStatus());
    }

    // returns true if both records in db for all files have been deleted and minio objects
    private boolean assertBlocksDeleted(List<String> hashList) throws Exception {
        userTransaction.begin();
        long count = dslContext.fetchCount(BLOCKS.where(BLOCKS.HASH.in(hashList)));
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

    private TasksRecord saveTask(List<String> filesToDelete) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("blocksToDelete", String.join(",", filesToDelete));

        return dslContext.insertInto(TASKS)
                .set(TASKS.ACTION, TaskType.DELETE_BLOCKS.name())
                .set(TASKS.METADATA, JSONB.valueOf(metadata.toString()))
                .set(TASKS.STATUS, TaskStatus.CREATED.name())
                .returning()
                .fetchOne();
    }

    private List<String> uploadFilesFromDirectoryToMinio(File directory) throws Exception {
        List<String> savedFiles = new ArrayList<>();

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                String filename = StringUtils.substringBeforeLast(file.getName(), ".");
                dslContext.insertInto(BLOCKS)
                        .set(BLOCKS.HASH, filename)
                        .execute();

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
