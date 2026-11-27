package com.sportflow.competition.exception;

import java.util.UUID;

public class EligibilityServiceUnavailableException extends RuntimeException {

    private final UUID registrationId;

    public EligibilityServiceUnavailableException(String message, UUID registrationId) {
        super(message);
        this.registrationId = registrationId;
    }

    public EligibilityServiceUnavailableException(String message, Throwable cause, UUID registrationId) {
        super(message, cause);
        this.registrationId = registrationId;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }
}
