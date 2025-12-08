package com.featureflag.service;

import com.featureflag.dto.EvaluationResponse;
import com.featureflag.dto.EvaluationResponse.EvaluationDetail;
import com.featureflag.dto.UserContext;
import com.featureflag.entity.FeatureFlag;
import com.featureflag.entity.FlagEvaluation;
import com.featureflag.entity.FlagRule;
import com.featureflag.enums.EvaluationReason;
import com.featureflag.enums.RuleType;
import com.featureflag.repository.FlagEvaluationRepository;
import com.featureflag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlagEvaluationService {

    private final FeatureFlagRepository flagRepository;
    private final FlagEvaluationRepository evaluationRepository;
    private final FlagCacheService cacheService;

    @Transactional(readOnly = true)
    public EvaluationResponse evaluateAllFlags(UserContext user) {
        List<FeatureFlag> flags = cacheService.getAllEnabledFlagsWithRules();

        Map<String, Boolean> results = new HashMap<>();
        Map<String, EvaluationDetail> details = new HashMap<>();

        for (FeatureFlag flag : flags) {
            EvaluationResult evalResult = evaluateFlag(flag, user);
            results.put(flag.getName(), evalResult.result);
            details.put(flag.getName(), evalResult.detail);

            // Log evaluation asynchronously
            logEvaluationAsync(flag, user.getUserId(), evalResult);
        }

        return EvaluationResponse.builder()
                .flags(results)
                .details(details)
                .build();
    }

    @Transactional(readOnly = true)
    public EvaluationResponse evaluateSingleFlag(String flagName, UserContext user) {
        FeatureFlag flag = cacheService.getFlagByNameWithRules(flagName);

        if (flag == null) {
            return EvaluationResponse.builder()
                    .flags(Map.of(flagName, false))
                    .details(Map.of(flagName, EvaluationDetail.builder()
                            .result(false)
                            .reason(EvaluationReason.FLAG_DISABLED)
                            .explanation("Flag not found: " + flagName)
                            .build()))
                    .build();
        }

        EvaluationResult evalResult = evaluateFlag(flag, user);
        logEvaluationAsync(flag, user.getUserId(), evalResult);

        return EvaluationResponse.builder()
                .flags(Map.of(flagName, evalResult.result))
                .details(Map.of(flagName, evalResult.detail))
                .build();
    }

    private EvaluationResult evaluateFlag(FeatureFlag flag, UserContext user) {
        // Check if flag is globally disabled
        if (!flag.isEnabled()) {
            return new EvaluationResult(
                    false,
                    EvaluationDetail.builder()
                            .result(false)
                            .reason(EvaluationReason.FLAG_DISABLED)
                            .explanation("Flag is globally disabled")
                            .build(),
                    null
            );
        }

        // Check targeting rules (in priority order)
        List<FlagRule> rules = flag.getRules();
        if (rules != null && !rules.isEmpty()) {
            // Sort by priority descending
            rules.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

            for (FlagRule rule : rules) {
                if (!rule.isEnabled()) continue;

                if (matchesRule(rule, user)) {
                    return new EvaluationResult(
                            true,
                            EvaluationDetail.builder()
                                    .result(true)
                                    .reason(EvaluationReason.RULE_MATCH)
                                    .matchedRuleId(rule.getId())
                                    .explanation(String.format("Matched rule: %s = %s",
                                            rule.getRuleType(), rule.getRuleValue()))
                                    .build(),
                            rule
                    );
                }
            }
        }

        // Fall back to percentage rollout
        boolean inRollout = isInRolloutPercentage(flag, user.getUserId());

        if (inRollout) {
            return new EvaluationResult(
                    true,
                    EvaluationDetail.builder()
                            .result(true)
                            .reason(EvaluationReason.ROLLOUT_INCLUDED)
                            .explanation(String.format("User included in %d%% rollout",
                                    flag.getRolloutPercentage()))
                            .build(),
                    null
            );
        } else {
            return new EvaluationResult(
                    false,
                    EvaluationDetail.builder()
                            .result(false)
                            .reason(EvaluationReason.ROLLOUT_EXCLUDED)
                            .explanation(String.format("User excluded from %d%% rollout",
                                    flag.getRolloutPercentage()))
                            .build(),
                    null
            );
        }
    }

    private boolean matchesRule(FlagRule rule, UserContext user) {
        return switch (rule.getRuleType()) {
            case USER_ID -> rule.getRuleValue().equals(user.getUserId());
            case EMAIL_EXACT -> user.getEmail() != null &&
                    rule.getRuleValue().equalsIgnoreCase(user.getEmail());
            case EMAIL_DOMAIN -> user.getEmail() != null &&
                    user.getEmail().toLowerCase().endsWith(rule.getRuleValue().toLowerCase());
            case COUNTRY -> user.getCountry() != null &&
                    rule.getRuleValue().equalsIgnoreCase(user.getCountry());
            case PERCENTAGE_GROUP -> isInPercentageGroup(rule.getRuleValue(), user.getUserId());
        };
    }

    private boolean isInRolloutPercentage(FeatureFlag flag, String userId) {
        if (flag.getRolloutPercentage() >= 100) return true;
        if (flag.getRolloutPercentage() <= 0) return false;

        // Generate deterministic hash from flag name + user ID
        // This ensures same user always gets same result for same flag
        String hashInput = flag.getName() + ":" + userId;
        int hash = Math.abs(hashInput.hashCode());
        int bucket = hash % 100;

        return bucket < flag.getRolloutPercentage();
    }

    private boolean isInPercentageGroup(String ruleValue, String userId) {
        try {
            int percentage = Integer.parseInt(ruleValue);
            int hash = Math.abs(userId.hashCode());
            int bucket = hash % 100;
            return bucket < percentage;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Async
    protected void logEvaluationAsync(FeatureFlag flag, String userId, EvaluationResult result) {
        try {
            FlagEvaluation evaluation = FlagEvaluation.builder()
                    .flag(flag)
                    .userId(userId)
                    .result(result.result)
                    .matchedRule(result.matchedRule)
                    .evaluationReason(result.detail.getReason())
                    .build();

            evaluationRepository.save(evaluation);
        } catch (Exception e) {
            log.error("Failed to log evaluation for flag {} and user {}: {}",
                    flag.getName(), userId, e.getMessage());
        }
    }

    private record EvaluationResult(boolean result, EvaluationDetail detail, FlagRule matchedRule) {}
}
