package com.featureflag.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlagRequest {

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Boolean enabled;

    @Min(value = 0, message = "Rollout percentage must be at least 0")
    @Max(value = 100, message = "Rollout percentage cannot exceed 100")
    private Integer rolloutPercentage;
}
