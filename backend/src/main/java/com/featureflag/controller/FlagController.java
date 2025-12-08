package com.featureflag.controller;

import com.featureflag.dto.*;
import com.featureflag.service.AnalyticsService;
import com.featureflag.service.FlagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/flags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FlagController {

    private final FlagService flagService;
    private final AnalyticsService analyticsService;

    @PostMapping
    public ResponseEntity<ApiResponse<FlagResponse>> createFlag(
            @Valid @RequestBody CreateFlagRequest request) {
        FlagResponse flag = flagService.createFlag(request);
        return ResponseEntity.ok(ApiResponse.success("Flag created", flag));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlagResponse>>> getAllFlags() {
        List<FlagResponse> flags = flagService.getAllFlags();
        return ResponseEntity.ok(ApiResponse.success(flags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlagResponse>> getFlag(@PathVariable UUID id) {
        FlagResponse flag = flagService.getFlag(id);
        return ResponseEntity.ok(ApiResponse.success(flag));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<FlagResponse>> getFlagByName(@PathVariable String name) {
        FlagResponse flag = flagService.getFlagByName(name);
        return ResponseEntity.ok(ApiResponse.success(flag));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlagResponse>> updateFlag(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFlagRequest request) {
        FlagResponse flag = flagService.updateFlag(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flag updated", flag));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleFlag(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        flagService.toggleFlag(id, enabled);
        return ResponseEntity.ok(ApiResponse.success("Flag toggled"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlag(@PathVariable UUID id) {
        flagService.deleteFlag(id);
        return ResponseEntity.ok(ApiResponse.success("Flag deleted"));
    }

    // Rules endpoints
    @PostMapping("/{flagId}/rules")
    public ResponseEntity<ApiResponse<RuleResponse>> addRule(
            @PathVariable UUID flagId,
            @Valid @RequestBody CreateRuleRequest request) {
        RuleResponse rule = flagService.addRule(flagId, request);
        return ResponseEntity.ok(ApiResponse.success("Rule added", rule));
    }

    @GetMapping("/{flagId}/rules")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getRules(@PathVariable UUID flagId) {
        List<RuleResponse> rules = flagService.getRules(flagId);
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @PatchMapping("/rules/{ruleId}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleRule(
            @PathVariable UUID ruleId,
            @RequestParam boolean enabled) {
        flagService.toggleRule(ruleId, enabled);
        return ResponseEntity.ok(ApiResponse.success("Rule toggled"));
    }

    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID ruleId) {
        flagService.deleteRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success("Rule deleted"));
    }

    // Analytics endpoint
    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "24") int hours) {
        AnalyticsResponse analytics = analyticsService.getAnalytics(id, hours);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
