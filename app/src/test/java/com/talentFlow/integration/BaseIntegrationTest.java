package com.talentFlow.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7.0");

    static {
        MONGODB.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGODB::getConnectionString);
        registry.add("EMAIL_USERNAME", () -> "dummy");
        registry.add("EMAIL_PASSWORD", () -> "dummy");
        registry.add("EMAIL_FROM", () -> "no-reply@test.local");
        registry.add("S3_BUCKET_ACCESS_KEY", () -> "dummy");
        registry.add("S3_BUCKET_SECRET_KEY", () -> "dummy");
        registry.add("S3_BUCKET_NAME", () -> "dummy-bucket");
    }
}