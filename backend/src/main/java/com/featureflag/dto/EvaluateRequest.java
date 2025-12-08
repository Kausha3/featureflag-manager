package com.featureflag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    private String userEmail;

    private String country;
}
