package com.featureflag.controller;

import com.featureflag.dto.ApiResponse;
import com.featureflag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final RedissonClient redissonClient;
    private final FeatureFlagRepository flagRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        // Check database
        try (Connection conn = dataSource.getConnection()) {
            health.put("database", conn.isValid(5) ? "UP" : "DOWN");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }

        // Check Redis
        try {
            redissonClient.getBucket("health-check").set("ok");
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redis_error", e.getMessage());
        }

        // Stats
        try {
            long totalFlags = flagRepository.count();
            long enabledFlags = flagRepository.countEnabled();
            health.put("stats", Map.of(
                    "total_flags", totalFlags,
                    "enabled_flags", enabledFlags
            ));
        } catch (Exception e) {
            health.put("stats_error", e.getMessage());
        }

        boolean isHealthy = "UP".equals(health.get("database")) && "UP".equals(health.get("redis"));
        health.put("status", isHealthy ? "UP" : "DEGRADED");

        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/ready")
    public ResponseEntity<String> readinessCheck() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                return ResponseEntity.ok("OK");
            }
        } catch (Exception e) {
            // Fall through
        }
        return ResponseEntity.status(503).body("NOT READY");
    }

    @GetMapping("/live")
    public ResponseEntity<String> livenessCheck() {
        return ResponseEntity.ok("OK");
    }
}
