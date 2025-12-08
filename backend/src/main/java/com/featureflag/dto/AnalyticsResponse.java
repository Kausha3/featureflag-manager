package com.featureflag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private UUID flagId;
    private String flagName;
    private long totalEvaluations;
    private long enabledCount;
    private long disabledCount;
    private double enabledPercentage;
    private int configuredRolloutPercentage;
    private List<TimeSeriesPoint> evaluationsOverTime;
    private Map<String, Long> evaluationsByReason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private Instant timestamp;
        private long enabledCount;
        private long disabledCount;
        private long totalCount;
    }
}
