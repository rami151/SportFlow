package com.sportflow.live.dto;

import jakarta.validation.constraints.NotBlank;

public record MatchStartRequest(
        @NotBlank(message = "Athlete A name is required")
        String athleteA,

        @NotBlank(message = "Athlete B name is required")
        String athleteB
) {}
