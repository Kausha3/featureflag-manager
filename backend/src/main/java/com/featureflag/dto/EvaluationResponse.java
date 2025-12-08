package com.featureflag.dto;

import com.featureflag.enums.EvaluationReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {

    private Map<String, Boolean> flags;
    private Map<String, EvaluationDetail> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationDetail {
        private boolean result;
        private EvaluationReason reason;
        private UUID matchedRuleId;
        private String explanation;
    }
}
