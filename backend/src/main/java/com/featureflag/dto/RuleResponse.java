package com.featureflag.dto;

import com.featureflag.entity.FlagRule;
import com.featureflag.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {

    private UUID id;
    private UUID flagId;
    private RuleType ruleType;
    private String ruleValue;
    private boolean enabled;
    private int priority;
    private Instant createdAt;
    private Instant updatedAt;

    public static RuleResponse fromEntity(FlagRule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .flagId(rule.getFlag().getId())
                .ruleType(rule.getRuleType())
                .ruleValue(rule.getRuleValue())
                .enabled(rule.isEnabled())
                .priority(rule.getPriority())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
