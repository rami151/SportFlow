package com.sportflow.competition.dto;

import com.sportflow.competition.entity.CompetitionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CompetitionUpdateRequest(
        @NotBlank(message = "Competition name is required")
        String name,

        @NotBlank(message = "Sport type is required")
        String sport,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Date is required")
        Instant date,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Max participants is required")
        @Min(value = 1, message = "Max participants must be at least 1")
        Integer maxParticipants,

        @NotNull(message = "Competition status is required")
        CompetitionStatus status
) {}
