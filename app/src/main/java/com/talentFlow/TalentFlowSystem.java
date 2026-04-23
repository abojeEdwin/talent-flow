package com.talentFlow;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TalentFlowSystem {
    public static void main(String[] args) {
        SpringApplication.run(TalentFlowSystem.class, args);
    }
}