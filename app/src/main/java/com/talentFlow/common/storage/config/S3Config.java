package com.talentFlow.common.storage.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public AmazonS3 amazonS3(S3Properties properties) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(
                properties.getS3BucketAccessKey(),
                properties.getS3BucketSecretKey()
        );
        return AmazonS3ClientBuilder.standard()
                .withRegion(properties.getS3BucketRegion())
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
