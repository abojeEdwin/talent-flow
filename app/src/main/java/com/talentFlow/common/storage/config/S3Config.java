package com.talentFlow.common.storage.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public AmazonS3 amazonS3(S3Properties properties) {
        String accessKey = trimToNull(properties.getS3BucketAccessKey());
        String secretKey = trimToNull(properties.getS3BucketSecretKey());
        String region = trimToNull(properties.getS3BucketRegion());
        if (region == null) {
            region = "us-east-1";
        }

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(region);

        if (accessKey != null && secretKey != null) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            return builder
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        }

        log.warn("S3 access keys not configured. Falling back to default AWS credential chain.");
        return builder
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
