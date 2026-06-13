package com.mymicroservice.authservice.configuration;

import com.mymicroservice.authservice.util.data.TestConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static com.mymicroservice.authservice.util.data.TestConstants.TEST_DB_NAME;
import static com.mymicroservice.authservice.util.data.TestConstants.TEST_DB_PASSWORD;
import static com.mymicroservice.authservice.util.data.TestConstants.TEST_DB_USER;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@Slf4j
public abstract class AbstractContainerTest {

    public static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(TestConstants.POSTGRES_IMAGE)
            .withDatabaseName(TEST_DB_NAME)
            .withUsername(TEST_DB_USER)
            .withPassword(TEST_DB_PASSWORD)
            .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofSeconds(120))
            );

    static {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbcUrl = postgreSQLContainer.getJdbcUrl();
        log.info("PostgreSQL URL: {}", jdbcUrl);
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        registry.add("spring.liquibase.enabled", () -> "false");
    }
}
