package com.sportflow.eligibility.domain;

public record EvaluationResult(boolean eligible, String reason) {

    public static final String OK = "OK";
    public static final String LICENSE_INVALID = "LICENSE_INVALID";
    public static final String AGE_OUT_OF_RANGE = "AGE_OUT_OF_RANGE";
    public static final String WEIGHT_OUT_OF_RANGE = "WEIGHT_OUT_OF_RANGE";
    public static final String CATEGORY_MISMATCH = "CATEGORY_MISMATCH";

    public static EvaluationResult ok() {
        return new EvaluationResult(true, OK);
    }

    public static EvaluationResult failure(String reason) {
        return new EvaluationResult(false, reason);
    }
}
