package com.sportflow.competition.exception;

public class CompetitionClosedException extends RuntimeException {
    public CompetitionClosedException(String message) {
        super(message);
    }
}
