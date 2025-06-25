package sanity.nil.block;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class QuarkusIntegrationTestResources implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> jdbcContainer;
    private GenericContainer<?> minioContainer;

    @Override
    public Map<String, String> start() {
        jdbcContainer = new PostgreSQLContainer<>("postgres:16.2-alpine")
                .withDatabaseName("jodrive")
                .withUsername("testUser")
                .withPassword("test");
        jdbcContainer.start();

        minioContainer = new GenericContainer<>("quay.io/minio/minio:latest")
                .withExposedPorts(9000)
                .withCommand("server --address :9000 /mnt/data")
                .withEnv("MINIO_ROOT_USER", "Q3AM3UQ867SPQQA43P2F")
                .withEnv("MINIO_ROOT_PASSWORD", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");
        minioContainer.start();

        Map<String, String> config = new HashMap<>();
        config.put("application.minio.url", minioContainer.getHost());
        config.put("application.minio.port", String.valueOf(minioContainer.getMappedPort(9000)));
        config.put("quarkus.datasource.jdbc.url", jdbcContainer.getJdbcUrl() + "&currentSchema=block_db");
        config.put("quarkus.datasource.username", jdbcContainer.getUsername());
        config.put("quarkus.datasource.password", jdbcContainer.getPassword());

        return config;
    }

    @Override
    public void stop() {
        if (jdbcContainer != null) jdbcContainer.stop();
        if (minioContainer != null) minioContainer.stop();
    }
}
