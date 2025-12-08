package com.featureflag.service;

import com.featureflag.dto.AnalyticsResponse;
import com.featureflag.dto.AnalyticsResponse.TimeSeriesPoint;
import com.featureflag.entity.FeatureFlag;
import com.featureflag.enums.EvaluationReason;
import com.featureflag.exception.FlagNotFoundException;
import com.featureflag.repository.FeatureFlagRepository;
import com.featureflag.repository.FlagEvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final FeatureFlagRepository flagRepository;
    private final FlagEvaluationRepository evaluationRepository;

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(UUID flagId) {
        return getAnalytics(flagId, 24); // Default to last 24 hours
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(UUID flagId, int hoursBack) {
        FeatureFlag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new FlagNotFoundException(flagId));

        Instant since = Instant.now().minus(hoursBack, ChronoUnit.HOURS);

        // Get total counts
        long totalEvaluations = evaluationRepository.countByFlagIdSince(flagId, since);
        long enabledCount = evaluationRepository.countByFlagIdAndResultSince(flagId, true, since);
        long disabledCount = evaluationRepository.countByFlagIdAndResultSince(flagId, false, since);

        // Calculate percentage
        double enabledPercentage = totalEvaluations > 0
                ? (double) enabledCount / totalEvaluations * 100
                : 0.0;

        // Get counts by reason
        Map<String, Long> evaluationsByReason = new HashMap<>();
        List<Object[]> reasonCounts = evaluationRepository.countByFlagIdGroupByReasonSince(flagId, since);
        for (Object[] row : reasonCounts) {
            EvaluationReason reason = (EvaluationReason) row[0];
            Long count = (Long) row[1];
            evaluationsByReason.put(reason.name(), count);
        }

        // Get time series data
        List<TimeSeriesPoint> timeSeries = new ArrayList<>();
        List<Object[]> hourlyStats = evaluationRepository.getHourlyEvaluationStats(flagId, since);
        for (Object[] row : hourlyStats) {
            Timestamp ts = (Timestamp) row[0];
            Long enabled = ((Number) row[1]).longValue();
            Long disabled = ((Number) row[2]).longValue();
            Long total = ((Number) row[3]).longValue();

            timeSeries.add(TimeSeriesPoint.builder()
                    .timestamp(ts.toInstant())
                    .enabledCount(enabled)
                    .disabledCount(disabled)
                    .totalCount(total)
                    .build());
        }

        return AnalyticsResponse.builder()
                .flagId(flagId)
                .flagName(flag.getName())
                .totalEvaluations(totalEvaluations)
                .enabledCount(enabledCount)
                .disabledCount(disabledCount)
                .enabledPercentage(Math.round(enabledPercentage * 100.0) / 100.0)
                .configuredRolloutPercentage(flag.getRolloutPercentage())
                .evaluationsOverTime(timeSeries)
                .evaluationsByReason(evaluationsByReason)
                .build();
    }

    @Transactional
    public int cleanupOldEvaluations(int daysToKeep) {
        Instant before = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        int deleted = evaluationRepository.deleteOldEvaluations(before);
        log.info("Deleted {} old evaluation records", deleted);
        return deleted;
    }
}
