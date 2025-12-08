package com.featureflag.repository;

import com.featureflag.entity.FlagEvaluation;
import com.featureflag.enums.EvaluationReason;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlagEvaluationRepository extends JpaRepository<FlagEvaluation, UUID> {

    long countByFlagId(UUID flagId);

    long countByFlagIdAndResult(UUID flagId, boolean result);

    @Query("SELECT COUNT(e) FROM FlagEvaluation e WHERE e.flag.id = :flagId AND e.evaluatedAt >= :since")
    long countByFlagIdSince(@Param("flagId") UUID flagId, @Param("since") Instant since);

    @Query("SELECT COUNT(e) FROM FlagEvaluation e WHERE e.flag.id = :flagId AND e.result = :result AND e.evaluatedAt >= :since")
    long countByFlagIdAndResultSince(@Param("flagId") UUID flagId, @Param("result") boolean result, @Param("since") Instant since);

    @Query("SELECT e.evaluationReason, COUNT(e) FROM FlagEvaluation e WHERE e.flag.id = :flagId GROUP BY e.evaluationReason")
    List<Object[]> countByFlagIdGroupByReason(@Param("flagId") UUID flagId);

    @Query("SELECT e.evaluationReason, COUNT(e) FROM FlagEvaluation e WHERE e.flag.id = :flagId AND e.evaluatedAt >= :since GROUP BY e.evaluationReason")
    List<Object[]> countByFlagIdGroupByReasonSince(@Param("flagId") UUID flagId, @Param("since") Instant since);

    List<FlagEvaluation> findByFlagIdOrderByEvaluatedAtDesc(UUID flagId, Pageable pageable);

    @Query(value = """
        SELECT
            date_trunc('hour', evaluated_at) as time_bucket,
            COUNT(*) FILTER (WHERE result = true) as enabled_count,
            COUNT(*) FILTER (WHERE result = false) as disabled_count,
            COUNT(*) as total_count
        FROM flag_evaluations
        WHERE flag_id = :flagId AND evaluated_at >= :since
        GROUP BY date_trunc('hour', evaluated_at)
        ORDER BY time_bucket
        """, nativeQuery = true)
    List<Object[]> getHourlyEvaluationStats(@Param("flagId") UUID flagId, @Param("since") Instant since);

    @Modifying
    @Query("DELETE FROM FlagEvaluation e WHERE e.evaluatedAt < :before")
    int deleteOldEvaluations(@Param("before") Instant before);

    @Modifying
    @Query("DELETE FROM FlagEvaluation e WHERE e.flag.id = :flagId")
    int deleteByFlagId(@Param("flagId") UUID flagId);
}
