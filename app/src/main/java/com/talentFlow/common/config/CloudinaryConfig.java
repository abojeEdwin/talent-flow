package com.talentFlow.common.config;

import com.cloudinary.Cloudinary;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties properties) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", required(properties.getCloudName(), "CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", required(properties.getApiKey(), "CLOUDINARY_API_KEY"));
        config.put("api_secret", required(properties.getApiSecret(), "CLOUDINARY_API_SECRET"));
        return new Cloudinary(config);
    }

    private String required(String value, String variableName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(variableName + " must be configured");
        }
        return value.trim();
    }
}
