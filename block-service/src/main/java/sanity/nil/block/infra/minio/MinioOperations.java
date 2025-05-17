//package sanity.nil.block.infra.minio;
//
//import io.minio.*;
//import io.minio.errors.ErrorResponseException;
//import io.minio.messages.DeleteError;
//import io.quarkus.runtime.Startup;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import lombok.extern.jbosslog.JBossLog;
//import org.eclipse.microprofile.config.inject.ConfigProperty;
//import sanity.nil.exceptions.StorageException;
//
//import java.util.stream.StreamSupport;
//
//@JBossLog
//@ApplicationScoped
//@Startup
//public class MinioOperations {
//
//    MinioClient minioClient;
//    public String DEFAULT_BUCKET;
//
//    @Inject
//    public MinioOperations(
//            @ConfigProperty(name = "application.minio.url") String url,
//            @ConfigProperty(name = "application.minio.port") Integer port,
//            @ConfigProperty(name = "application.minio.accessKey") String accessKey,
//            @ConfigProperty(name = "application.minio.secretKey") String secretKey,
//            @ConfigProperty(name = "application.minio.bucket", defaultValue = "blocks") String bucket
//    ) {
//        this.DEFAULT_BUCKET = bucket;
//        this.minioClient = MinioClient.builder()
//                .endpoint(url, port, false)
//                .credentials(accessKey, secretKey)
//                .build();
//
//        log.info("Minio on " + url + port + " initialized");
//        createBucketIfNotExists(DEFAULT_BUCKET);
//        log.debug(DEFAULT_BUCKET + " bucket exists");
//    }
//
//    public void createBucketIfNotExists(String bucketName) {
//        try {
//            if (!minioClient.bucketExists(BucketExistsArgs.builder()
//                    .bucket(bucketName)
//                    .build())) {
//
//                minioClient.makeBucket(MakeBucketArgs.builder()
//                        .bucket(bucketName)
//                        .build());
//                log.info("Created minio bucket with name " + bucketName);
//            }
//        } catch (ErrorResponseException e) {
//            throw new StorageException(getMinioErrCode(e));
//        } catch (Exception e) {
//            throw new StorageException(e.getMessage());
//        }
//    }
//
//    public void putObject(PutObjectArgs.Builder argsBuilder) throws StorageException {
//        try {
//            var response = minioClient.putObject(
//                    argsBuilder.bucket(DEFAULT_BUCKET).build()
//            );
//            if (response.etag() == null) {
//                throw new StorageException("Error uploading object: " + response.etag());
//            }
//        } catch (ErrorResponseException e) {
//            throw new StorageException(getMinioErrCode(e));
//        } catch (Exception e) {
//            throw new StorageException(e.getMessage());
//        }
//    }
//
//    public int removeObjects(RemoveObjectsArgs.Builder argsBuilder) throws StorageException {
//        try {
//            var objectsToDelete = minioClient.removeObjects(
//                    argsBuilder.bucket(DEFAULT_BUCKET).build()
//            );
//            int deleted = 0;
//            log.info("Objects to delete " + StreamSupport.stream(objectsToDelete.spliterator(), false).count());
//            if (objectsToDelete != null) {
//                for (var object : objectsToDelete) {
//                    DeleteError error = object.get();
//                    log.info("Deleting object " + error.objectName() + " with code " + error.code());
//                    deleted++;
//                }
//                return deleted;
//            }
//        } catch (ErrorResponseException e) {
//            throw new StorageException(getMinioErrCode(e));
//        } catch (Exception e) {
//            throw new StorageException(e.getMessage());
//        }
//        return 0;
//    }
//
//    public GetObjectResponse getObject(String objectName) throws StorageException {
//        try {
//            return minioClient.getObject(
//                    GetObjectArgs.builder().bucket(DEFAULT_BUCKET).object(objectName).build()
//            );
//        } catch (ErrorResponseException e) {
//            throw new StorageException(getMinioErrCode(e));
//        } catch (Exception e) {
//            throw new StorageException(e.getMessage());
//        }
//    }
//
//    public StatObjectResponse statObject(String objectName) throws StorageException {
//        try {
//            return minioClient.statObject(
//                    StatObjectArgs.builder()
//                            .bucket(DEFAULT_BUCKET)
//                            .object(objectName)
//                            .build()
//            );
//        } catch (ErrorResponseException e) {
//            throw new StorageException(getMinioErrCode(e));
//        } catch (Exception e) {
//            throw new StorageException(e.getMessage());
//        }
//    }
//
//    private String getMinioErrCode(ErrorResponseException e) {
//        return String.format("%s : %s", e.errorResponse().code(), e.getMessage());
//    }
//}
