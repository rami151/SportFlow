package com.sportflow.live.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScoreUpdateRequest(
        @NotBlank(message = "Athlete A name is required")
        String athleteA,

        @NotBlank(message = "Athlete B name is required")
        String athleteB,

        @NotNull(message = "Score A is required")
        @Min(value = 0, message = "Score A cannot be negative")
        Integer scoreA,

        @NotNull(message = "Score B is required")
        @Min(value = 0, message = "Score B cannot be negative")
        Integer scoreB
) {}
