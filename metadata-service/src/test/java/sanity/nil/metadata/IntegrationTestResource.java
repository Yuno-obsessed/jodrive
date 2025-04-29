package sanity.nil.metadata;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class IntegrationTestResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> jdbcContainer;

    @Override
    public Map<String, String> start() {
        jdbcContainer = new PostgreSQLContainer<>("postgres:16.2-alpine")
                .withDatabaseName("jodrive")
                .withUsername("testUser")
                .withPassword("test");
//                .withInitScript("init.sql");
        jdbcContainer.start();

        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.jdbc.url", jdbcContainer.getJdbcUrl());
        config.put("quarkus.datasource.username", jdbcContainer.getUsername());
        config.put("quarkus.datasource.password", jdbcContainer.getPassword());

        return config;
    }

    @Override
    public void stop() {
        if (jdbcContainer != null) jdbcContainer.stop();
    }
}
