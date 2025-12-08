package com.featureflag.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Value("${redis.address:redis://localhost:6379}")
    private String redisAddress;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        var serverConfig = config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(redisDatabase)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(24)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        if (redisPassword != null && !redisPassword.isBlank()) {
            serverConfig.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
