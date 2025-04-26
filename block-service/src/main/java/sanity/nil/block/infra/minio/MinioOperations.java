package sanity.nil.block.infra.minio;

import io.minio.*;
import io.minio.messages.DeleteError;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import sanity.nil.exceptions.StorageException;

import java.util.stream.StreamSupport;

@JBossLog
@ApplicationScoped
@Startup
public class MinioOperations {

    MinioClient minioClient;

    @Inject
    public MinioOperations(
            @ConfigProperty(name = "application.minio.url") String url,
            @ConfigProperty(name = "application.minio.port") Integer port,
            @ConfigProperty(name = "application.minio.accessKey") String accessKey,
            @ConfigProperty(name = "application.minio.secretKey") String secretKey) {

        this.minioClient = MinioClient.builder()
                .endpoint(url, port, false)
                .credentials(accessKey, secretKey)
                .build();

        log.info("Minio on " + url + port + " initialized");
    }

    public void createBucketIfNotExists(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build())) {

                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created minio bucket with name " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());

        }
    }

    public void putObject(PutObjectArgs putObjectArgs) throws StorageException {
        try {
            var response = minioClient.putObject(putObjectArgs);
            if (response.etag() == null) {
                throw new StorageException("Error uploading object: " + response.etag());
            }
        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
    }

    public int removeObjects(RemoveObjectsArgs removeObjectsArgs) throws StorageException {
        try {
            var objectsToDelete = minioClient.removeObjects(removeObjectsArgs);
            int deleted = 0;
            log.info("Objects to delete " + StreamSupport.stream(objectsToDelete.spliterator(), false).count());
            if (objectsToDelete != null) {
                for (var object : objectsToDelete) {
                    DeleteError error = object.get();
                    log.info("Deleting object " + error.objectName() + " with code " + error.code());
                    deleted++;
                }
                return deleted;
            }
        } catch (Exception e) {
            throw new StorageException(e.getMessage());
        }
        return 0;
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }
}
