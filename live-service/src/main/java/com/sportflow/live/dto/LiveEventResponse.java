package com.sportflow.live.dto;

import java.time.Instant;
import java.util.UUID;

public record LiveEventResponse(
        UUID id,
        String competitionId,
        String eventType,
        String athleteA,
        String athleteB,
        int scoreA,
        int scoreB,
        long eventTimestamp,
        Instant createdAt
) {}
