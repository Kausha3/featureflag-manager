package com.featureflag.dto;

import com.featureflag.enums.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRuleRequest {

    @NotNull(message = "Rule type is required")
    private RuleType ruleType;

    @NotBlank(message = "Rule value is required")
    private String ruleValue;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private int priority = 0;
}
