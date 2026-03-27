package com.talentFlow.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("talent_flow_test")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("EMAIL_USERNAME", () -> "dummy");
        registry.add("EMAIL_PASSWORD", () -> "dummy");
        registry.add("EMAIL_FROM", () -> "no-reply@test.local");
        registry.add("S3_BUCKET_ACCESS_KEY", () -> "dummy");
        registry.add("S3_BUCKET_SECRET_KEY", () -> "dummy");
        registry.add("S3_BUCKET_NAME", () -> "dummy-bucket");
    }
}
