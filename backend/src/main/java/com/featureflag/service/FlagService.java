package com.featureflag.service;

import com.featureflag.dto.*;
import com.featureflag.entity.FeatureFlag;
import com.featureflag.entity.FlagRule;
import com.featureflag.exception.FlagNotFoundException;
import com.featureflag.exception.DuplicateFlagException;
import com.featureflag.exception.DuplicateRuleException;
import com.featureflag.repository.FeatureFlagRepository;
import com.featureflag.repository.FlagRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlagService {

    private final FeatureFlagRepository flagRepository;
    private final FlagRuleRepository ruleRepository;
    private final FlagCacheService cacheService;

    @Transactional
    public FlagResponse createFlag(CreateFlagRequest request) {
        if (flagRepository.existsByName(request.getName())) {
            throw new DuplicateFlagException("Flag already exists: " + request.getName());
        }

        FeatureFlag flag = FeatureFlag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .enabled(request.isEnabled())
                .rolloutPercentage(request.getRolloutPercentage())
                .createdBy(request.getCreatedBy())
                .build();

        flag = flagRepository.save(flag);
        cacheService.invalidateCache();

        log.info("Created flag: {} by {}", flag.getName(), request.getCreatedBy());
        return FlagResponse.fromEntity(flag);
    }

    @Transactional(readOnly = true)
    public FlagResponse getFlag(UUID id) {
        FeatureFlag flag = flagRepository.findByIdWithRules(id)
                .orElseThrow(() -> new FlagNotFoundException(id));
        return FlagResponse.fromEntity(flag);
    }

    @Transactional(readOnly = true)
    public FlagResponse getFlagByName(String name) {
        FeatureFlag flag = flagRepository.findByNameWithRules(name)
                .orElseThrow(() -> new FlagNotFoundException(name));
        return FlagResponse.fromEntity(flag);
    }

    @Transactional(readOnly = true)
    public List<FlagResponse> getAllFlags() {
        return flagRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FlagResponse::fromEntityWithoutRules)
                .collect(Collectors.toList());
    }

    @Transactional
    public FlagResponse updateFlag(UUID id, UpdateFlagRequest request) {
        FeatureFlag flag = flagRepository.findById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        if (request.getDescription() != null) {
            flag.setDescription(request.getDescription());
        }
        if (request.getEnabled() != null) {
            flag.setEnabled(request.getEnabled());
        }
        if (request.getRolloutPercentage() != null) {
            flag.setRolloutPercentage(request.getRolloutPercentage());
        }

        flag = flagRepository.save(flag);
        cacheService.invalidateCache();

        log.info("Updated flag: {}", flag.getName());
        return FlagResponse.fromEntity(flag);
    }

    @Transactional
    public void toggleFlag(UUID id, boolean enabled) {
        FeatureFlag flag = flagRepository.findById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        flag.setEnabled(enabled);
        flagRepository.save(flag);
        cacheService.invalidateCache();

        log.info("Toggled flag {} to {}", flag.getName(), enabled);
    }

    @Transactional
    public void deleteFlag(UUID id) {
        FeatureFlag flag = flagRepository.findById(id)
                .orElseThrow(() -> new FlagNotFoundException(id));

        flagRepository.delete(flag);
        cacheService.invalidateCache();

        log.info("Deleted flag: {}", flag.getName());
    }

    // Rule management
    @Transactional
    public RuleResponse addRule(UUID flagId, CreateRuleRequest request) {
        FeatureFlag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new FlagNotFoundException(flagId));

        if (ruleRepository.existsByFlagIdAndRuleTypeAndRuleValue(
                flagId, request.getRuleType(), request.getRuleValue())) {
            throw new DuplicateRuleException("Rule already exists for this flag");
        }

        FlagRule rule = FlagRule.builder()
                .flag(flag)
                .ruleType(request.getRuleType())
                .ruleValue(request.getRuleValue())
                .enabled(request.isEnabled())
                .priority(request.getPriority())
                .build();

        rule = ruleRepository.save(rule);
        cacheService.invalidateCache();

        log.info("Added rule to flag {}: {} = {}", flag.getName(),
                request.getRuleType(), request.getRuleValue());
        return RuleResponse.fromEntity(rule);
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> getRules(UUID flagId) {
        if (!flagRepository.existsById(flagId)) {
            throw new FlagNotFoundException(flagId);
        }

        return ruleRepository.findByFlagIdOrderByPriorityDesc(flagId).stream()
                .map(RuleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleRule(UUID ruleId, boolean enabled) {
        FlagRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));

        rule.setEnabled(enabled);
        ruleRepository.save(rule);
        cacheService.invalidateCache();

        log.info("Toggled rule {} to {}", ruleId, enabled);
    }

    @Transactional
    public void deleteRule(UUID ruleId) {
        FlagRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));

        ruleRepository.delete(rule);
        cacheService.invalidateCache();

        log.info("Deleted rule: {}", ruleId);
    }
}
