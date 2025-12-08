package com.featureflag.controller;

import com.featureflag.dto.*;
import com.featureflag.service.FlagEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/flags")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EvaluationController {

    private final FlagEvaluationService evaluationService;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluateFlags(
            @Valid @RequestBody EvaluateRequest request) {
        UserContext userContext = UserContext.builder()
                .userId(request.getUserId())
                .email(request.getUserEmail())
                .country(request.getCountry())
                .build();

        EvaluationResponse response = evaluationService.evaluateAllFlags(userContext);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/evaluate/{flagName}")
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluateSingleFlag(
            @PathVariable String flagName,
            @Valid @RequestBody EvaluateRequest request) {
        UserContext userContext = UserContext.builder()
                .userId(request.getUserId())
                .email(request.getUserEmail())
                .country(request.getCountry())
                .build();

        EvaluationResponse response = evaluationService.evaluateSingleFlag(flagName, userContext);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Simple GET endpoint for quick evaluation
    @GetMapping("/evaluate")
    public ResponseEntity<ApiResponse<EvaluationResponse>> evaluateFlagsGet(
            @RequestParam String userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String country) {
        UserContext userContext = UserContext.builder()
                .userId(userId)
                .email(email)
                .country(country)
                .build();

        EvaluationResponse response = evaluationService.evaluateAllFlags(userContext);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
