package com.sportflow.competition.dto;

import com.sportflow.competition.entity.CompetitionStatus;
import java.time.Instant;
import java.util.UUID;

public record CompetitionResponse(
        UUID id,
        String name,
        String sport,
        String location,
        Instant date,
        String category,
        int maxParticipants,
        CompetitionStatus status,
        long currentParticipants,
        Instant createdAt
) {}
