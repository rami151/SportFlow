package com.sportflow.eligibility.domain;

import com.sportflow.eligibility.config.EligibilityProperties;
import org.springframework.stereotype.Component;

/**
 * Pure Java business rule evaluation for athlete eligibility.
 * Contains no gRPC types.
 */
@Component
public class EligibilityRules {

    private final EligibilityProperties properties;

    public EligibilityRules(EligibilityProperties properties) {
        this.properties = properties;
    }

    public EvaluationResult evaluate(int age, double weight, String licenseNumber, String category) {
        // 1. License format check
        if (licenseNumber == null || licenseNumber.isBlank()) {
            return EvaluationResult.failure(EvaluationResult.LICENSE_INVALID);
        }

        // 2. Global age check (between minAge and maxAge inclusive)
        if (age < properties.getMinAge() || age > properties.getMaxAge()) {
            return EvaluationResult.failure(EvaluationResult.AGE_OUT_OF_RANGE);
        }

        // 3. Global weight check (between minWeight and maxWeight inclusive)
        if (weight < properties.getMinWeight() || weight > properties.getMaxWeight()) {
            return EvaluationResult.failure(EvaluationResult.WEIGHT_OUT_OF_RANGE);
        }

        // 4. Category validity check
        var optCategory = Category.fromString(category);
        if (optCategory.isEmpty()) {
            return EvaluationResult.failure(EvaluationResult.CATEGORY_MISMATCH);
        }

        Category cat = optCategory.get();
        boolean matchesGroup = switch (cat.getGroup()) {
            case JUNIOR -> age < properties.getSeniorMinAge();
            case SENIOR -> age >= properties.getSeniorMinAge() && age <= properties.getSeniorMaxAge();
            case VETERAN -> age > properties.getSeniorMaxAge();
        };

        if (!matchesGroup) {
            return EvaluationResult.failure(EvaluationResult.CATEGORY_MISMATCH);
        }

        // All checks passed
        return EvaluationResult.ok();
    }
}
