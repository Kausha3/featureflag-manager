package com.featureflag.dto;

import com.featureflag.entity.FeatureFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagResponse {

    private UUID id;
    private String name;
    private String description;
    private boolean enabled;
    private int rolloutPercentage;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<RuleResponse> rules;
    private long rulesCount;

    public static FlagResponse fromEntity(FeatureFlag flag) {
        return FlagResponse.builder()
                .id(flag.getId())
                .name(flag.getName())
                .description(flag.getDescription())
                .enabled(flag.isEnabled())
                .rolloutPercentage(flag.getRolloutPercentage())
                .createdBy(flag.getCreatedBy())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .rules(flag.getRules() != null ?
                        flag.getRules().stream()
                                .map(RuleResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .rulesCount(flag.getRules() != null ? flag.getRules().size() : 0)
                .build();
    }

    public static FlagResponse fromEntityWithoutRules(FeatureFlag flag) {
        return FlagResponse.builder()
                .id(flag.getId())
                .name(flag.getName())
                .description(flag.getDescription())
                .enabled(flag.isEnabled())
                .rolloutPercentage(flag.getRolloutPercentage())
                .createdBy(flag.getCreatedBy())
                .createdAt(flag.getCreatedAt())
                .updatedAt(flag.getUpdatedAt())
                .build();
    }
}
