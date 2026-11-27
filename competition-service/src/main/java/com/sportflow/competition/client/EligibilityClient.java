package com.sportflow.competition.client;

import java.util.UUID;

public interface EligibilityClient {
    EligibilityCheckResult checkEligibility(
            UUID athleteId,
            UUID competitionId,
            int athleteAge,
            double athleteWeight,
            String licenseNumber,
            String category
    );
}
