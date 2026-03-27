package com.talentFlow.common.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.s3")
public class S3Properties {

    private String s3BucketAccessKey;

    private String s3BucketSecretKey;

    private String s3BucketRegion;

    private String s3BucketName;
}
