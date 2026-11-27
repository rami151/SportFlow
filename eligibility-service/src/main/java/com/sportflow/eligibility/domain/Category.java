package com.sportflow.eligibility.domain;

import java.util.Arrays;
import java.util.Optional;

public enum Category {
    JUNIOR(CategoryGroup.JUNIOR),
    SENIOR(CategoryGroup.SENIOR),
    VETERAN(CategoryGroup.VETERAN),

    // Weight class extensions
    JUNIOR_LIGHTWEIGHT(CategoryGroup.JUNIOR),
    JUNIOR_MIDDLEWEIGHT(CategoryGroup.JUNIOR),
    JUNIOR_HEAVYWEIGHT(CategoryGroup.JUNIOR),

    SENIOR_LIGHTWEIGHT(CategoryGroup.SENIOR),
    SENIOR_MIDDLEWEIGHT(CategoryGroup.SENIOR),
    SENIOR_HEAVYWEIGHT(CategoryGroup.SENIOR),

    VETERAN_LIGHTWEIGHT(CategoryGroup.VETERAN),
    VETERAN_MIDDLEWEIGHT(CategoryGroup.VETERAN),
    VETERAN_HEAVYWEIGHT(CategoryGroup.VETERAN);

    public enum CategoryGroup {
        JUNIOR,
        SENIOR,
        VETERAN
    }

    private final CategoryGroup group;

    Category(CategoryGroup group) {
        this.group = group;
    }

    public CategoryGroup getGroup() {
        return group;
    }

    public static Optional<Category> fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        return Arrays.stream(values())
                .filter(c -> c.name().equals(normalized))
                .findFirst();
    }
}
