package com.featureflag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeatureFlagApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagApplication.class, args);
    }
}
