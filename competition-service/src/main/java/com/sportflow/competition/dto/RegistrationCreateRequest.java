package com.sportflow.competition.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegistrationCreateRequest(
        @NotNull(message = "Athlete ID is required")
        UUID athleteId,

        @NotNull(message = "Athlete age is required")
        @Min(value = 1, message = "Athlete age must be positive")
        Integer athleteAge,

        @NotNull(message = "Athlete weight is required")
        @DecimalMin(value = "1.0", message = "Athlete weight must be positive")
        Double athleteWeight,

        @NotBlank(message = "License number is required")
        String licenseNumber,

        @NotBlank(message = "Category is required")
        String category
) {}
