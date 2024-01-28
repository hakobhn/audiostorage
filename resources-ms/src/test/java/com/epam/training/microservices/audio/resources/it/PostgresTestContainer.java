package com.epam.training.microservices.audio.resources.it;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("it")
// Tells Spring not to replace the application default DataSource.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public interface PostgresTestContainer {

    String DOCKER_IMAGE_NAME = "postgres:15";

    @Container
    PostgreSQLContainer<?> container =
            new PostgreSQLContainer<>(DOCKER_IMAGE_NAME);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }
}