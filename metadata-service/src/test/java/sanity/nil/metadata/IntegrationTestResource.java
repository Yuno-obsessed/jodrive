package sanity.nil.metadata;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.extern.jbosslog.JBossLog;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

@JBossLog
public class IntegrationTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> jdbcContainer;
    private GenericContainer<?> redisContainer;
    private GenericContainer<?> minioContainer;

    @Override
    public Map<String, String> start() {
        jdbcContainer = new PostgreSQLContainer<>("postgres:16.2-alpine")
                .withDatabaseName("jodrive")
                .withUsername("testUser")
                .withPassword("test");
        redisContainer = new GenericContainer<>("redis:alpine")
                .withExposedPorts(6379)
                .withCommand("redis-server");
        minioContainer = new GenericContainer<>("quay.io/minio/minio:latest")
                .withExposedPorts(9000)
                .withCommand("server --address :9000 /mnt/data")
                .withEnv("MINIO_ROOT_USER", "Q3AM3UQ867SPQQA43P2F")
                .withEnv("MINIO_ROOT_PASSWORD", "zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG");

        minioContainer.start();
        jdbcContainer.start();
        redisContainer.start();

        String redisHost = redisContainer.getHost();
        Integer redisPort = redisContainer.getMappedPort(6379);
        String redisUrl = "redis://:@" + redisHost + ":" + redisPort + "/";
        String pgUrl = jdbcContainer.getJdbcUrl() + "&currentSchema=metadata_db";

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.jdbc.url", pgUrl);
        config.put("quarkus.datasource.username", jdbcContainer.getUsername());
        config.put("quarkus.datasource.password", jdbcContainer.getPassword());
        config.put("quarkus.redis.hosts", redisUrl);
        config.put("application.minio.url", minioContainer.getHost());
        config.put("application.minio.port", String.valueOf(minioContainer.getMappedPort(9000)));

        log.info("Postgres started at url: " + pgUrl);
        log.info("Redis started at url: " + redisUrl);
        log.info("Minio started at url: " + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000));
        return config;
    }

    @Override
    public void stop() {
        if (jdbcContainer != null) jdbcContainer.stop();
        if (redisContainer != null) redisContainer.stop();
        if (minioContainer != null) minioContainer.stop();
    }
}
