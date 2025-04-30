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

    @Override
    public Map<String, String> start() {
        jdbcContainer = new PostgreSQLContainer<>("postgres:16.2-alpine")
                .withDatabaseName("jodrive")
                .withUsername("testUser")
                .withPassword("test");
        redisContainer = new GenericContainer<>("redis:alpine")
                .withExposedPorts(6379)
                .withCommand("redis-server");

        jdbcContainer.start();
        redisContainer.start();

        String redisHost = redisContainer.getHost();
        Integer redisPort = redisContainer.getMappedPort(6379);
        String redisUrl = "redis://:@" + redisHost + ":" + redisPort + "/";
        String pgUrl = jdbcContainer.getJdbcUrl();

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.jdbc.url", pgUrl);
        config.put("quarkus.datasource.username", jdbcContainer.getUsername());
        config.put("quarkus.datasource.password", jdbcContainer.getPassword());
        config.put("quarkus.redis.hosts", redisUrl);

        log.info("Postgres started at url: " + pgUrl);
        log.info("Redis started at url: " + redisUrl);
        return config;
    }

    @Override
    public void stop() {
        if (jdbcContainer != null) jdbcContainer.stop();
        if (redisContainer != null) redisContainer.stop();
    }
}
