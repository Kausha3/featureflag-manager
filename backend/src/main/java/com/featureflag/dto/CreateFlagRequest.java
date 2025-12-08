package com.featureflag.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlagRequest {

    @NotBlank(message = "Flag name is required")
    @Size(min = 2, max = 100, message = "Flag name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "Flag name must start with lowercase letter and contain only lowercase letters, numbers, and underscores")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private boolean enabled;

    @Min(value = 0, message = "Rollout percentage must be at least 0")
    @Max(value = 100, message = "Rollout percentage cannot exceed 100")
    private int rolloutPercentage;

    private String createdBy;
}
