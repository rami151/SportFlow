package com.sportflow.competition.client;

public record EligibilityCheckResult(boolean eligible, String reason) {

    public static EligibilityCheckResult success() {
        return new EligibilityCheckResult(true, "OK");
    }

    public static EligibilityCheckResult failure(String reason) {
        return new EligibilityCheckResult(false, reason);
    }
}
