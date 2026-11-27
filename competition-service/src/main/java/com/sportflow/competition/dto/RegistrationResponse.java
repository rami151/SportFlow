package com.sportflow.competition.dto;

import com.sportflow.competition.entity.RegistrationStatus;
import java.time.Instant;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID competitionId,
        UUID athleteId,
        int athleteAge,
        double athleteWeight,
        String licenseNumber,
        String category,
        RegistrationStatus status,
        String rejectionReason,
        Instant createdAt
) {}
