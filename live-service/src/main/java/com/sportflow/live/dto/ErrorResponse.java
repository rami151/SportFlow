package com.sportflow.live.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String message,
        List<String> details
) {
    public ErrorResponse(int status, String message) {
        this(Instant.now(), status, message, List.of());
    }

    public ErrorResponse(int status, String message, List<String> details) {
        this(Instant.now(), status, message, details);
    }
}
