package com.featureflag.entity;

import com.featureflag.enums.EvaluationReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "flag_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlagEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id", nullable = false)
    private FeatureFlag flag;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private boolean result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_rule_id")
    private FlagRule matchedRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_reason", nullable = false, length = 50)
    private EvaluationReason evaluationReason;

    @CreationTimestamp
    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private Instant evaluatedAt;
}
