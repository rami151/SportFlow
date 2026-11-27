package com.sportflow.eligibility.domain;

import com.sportflow.eligibility.config.EligibilityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityRulesTest {

    private EligibilityRules eligibilityRules;
    private EligibilityProperties properties;

    @BeforeEach
    void setUp() {
        properties = new EligibilityProperties();
        properties.setMinAge(6);
        properties.setMaxAge(80);
        properties.setMinWeight(20.0);
        properties.setMaxWeight(200.0);
        properties.setJuniorMaxAge(17);
        properties.setSeniorMinAge(18);
        properties.setSeniorMaxAge(35);
        properties.setVeteranMinAge(36);

        eligibilityRules = new EligibilityRules(properties);
    }

    @Nested
    @DisplayName("Valid Eligibility Scenarios")
    class ValidScenarios {

        @Test
        @DisplayName("Valid JUNIOR athlete passes")
        void validJunior() {
            EvaluationResult result = eligibilityRules.evaluate(16, 55.0, "LIC-12345", "JUNIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Valid SENIOR athlete passes")
        void validSenior() {
            EvaluationResult result = eligibilityRules.evaluate(25, 75.0, "LIC-99999", "SENIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Valid VETERAN athlete passes")
        void validVeteran() {
            EvaluationResult result = eligibilityRules.evaluate(45, 82.5, "LIC-88888", "VETERAN");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Valid weight class extension passes")
        void validWeightClassCategory() {
            EvaluationResult result = eligibilityRules.evaluate(28, 68.0, "LIC-77777", "SENIOR_LIGHTWEIGHT");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Case insensitive and hyphenated category passes")
        void validFormattedCategory() {
            EvaluationResult result = eligibilityRules.evaluate(28, 68.0, "LIC-77777", "senior-lightweight");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }
    }

    @Nested
    @DisplayName("License Validation")
    class LicenseValidation {

        @Test
        @DisplayName("Null license returns LICENSE_INVALID")
        void nullLicense() {
            EvaluationResult result = eligibilityRules.evaluate(25, 70.0, null, "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.LICENSE_INVALID);
        }

        @Test
        @DisplayName("Empty license returns LICENSE_INVALID")
        void emptyLicense() {
            EvaluationResult result = eligibilityRules.evaluate(25, 70.0, "", "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.LICENSE_INVALID);
        }

        @Test
        @DisplayName("Blank whitespace license returns LICENSE_INVALID")
        void blankLicense() {
            EvaluationResult result = eligibilityRules.evaluate(25, 70.0, "   ", "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.LICENSE_INVALID);
        }
    }

    @Nested
    @DisplayName("Age Boundary Validation")
    class AgeBoundaries {

        @Test
        @DisplayName("Lower age boundary (6) with JUNIOR is valid")
        void lowerAgeBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(6, 25.0, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Below lower age boundary (5) returns AGE_OUT_OF_RANGE")
        void belowLowerAgeBoundary() {
            EvaluationResult result = eligibilityRules.evaluate(5, 25.0, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.AGE_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("Upper age boundary (80) with VETERAN is valid")
        void upperAgeBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(80, 70.0, "LIC-001", "VETERAN");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Above upper age boundary (81) returns AGE_OUT_OF_RANGE")
        void aboveUpperAgeBoundary() {
            EvaluationResult result = eligibilityRules.evaluate(81, 70.0, "LIC-001", "VETERAN");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.AGE_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("Weight Boundary Validation")
    class WeightBoundaries {

        @Test
        @DisplayName("Lower weight boundary (20.0 kg) is valid")
        void lowerWeightBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(10, 20.0, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Below lower weight boundary (19.9 kg) returns WEIGHT_OUT_OF_RANGE")
        void belowLowerWeightBoundary() {
            EvaluationResult result = eligibilityRules.evaluate(10, 19.9, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.WEIGHT_OUT_OF_RANGE);
        }

        @Test
        @DisplayName("Upper weight boundary (200.0 kg) is valid")
        void upperWeightBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(30, 200.0, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("Above upper weight boundary (200.1 kg) returns WEIGHT_OUT_OF_RANGE")
        void aboveUpperWeightBoundary() {
            EvaluationResult result = eligibilityRules.evaluate(30, 200.1, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.WEIGHT_OUT_OF_RANGE);
        }
    }

    @Nested
    @DisplayName("Category and Age Consistency Validation")
    class CategoryConsistency {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "UNKNOWN", "CADET", "MASTER", "NOVICE"})
        @DisplayName("Unknown or invalid category returns CATEGORY_MISMATCH")
        void unknownCategory(String category) {
            EvaluationResult result = eligibilityRules.evaluate(25, 70.0, "LIC-001", category);
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("Null category returns CATEGORY_MISMATCH")
        void nullCategory() {
            EvaluationResult result = eligibilityRules.evaluate(25, 70.0, "LIC-001", null);
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("JUNIOR upper age boundary (17) is valid")
        void juniorUpperBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(17, 60.0, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("JUNIOR category with age 18 returns CATEGORY_MISMATCH")
        void juniorWithAge18Mismatch() {
            EvaluationResult result = eligibilityRules.evaluate(18, 60.0, "LIC-001", "JUNIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("SENIOR lower age boundary (18) is valid")
        void seniorLowerBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(18, 70.0, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("SENIOR upper age boundary (35) is valid")
        void seniorUpperBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(35, 70.0, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("SENIOR with age 17 returns CATEGORY_MISMATCH")
        void seniorWithAge17Mismatch() {
            EvaluationResult result = eligibilityRules.evaluate(17, 70.0, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("SENIOR with age 36 returns CATEGORY_MISMATCH")
        void seniorWithAge36Mismatch() {
            EvaluationResult result = eligibilityRules.evaluate(36, 70.0, "LIC-001", "SENIOR");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }

        @Test
        @DisplayName("VETERAN lower age boundary (36) is valid")
        void veteranLowerBoundaryValid() {
            EvaluationResult result = eligibilityRules.evaluate(36, 80.0, "LIC-001", "VETERAN");
            assertThat(result.eligible()).isTrue();
            assertThat(result.reason()).isEqualTo(EvaluationResult.OK);
        }

        @Test
        @DisplayName("VETERAN with age 35 returns CATEGORY_MISMATCH")
        void veteranWithAge35Mismatch() {
            EvaluationResult result = eligibilityRules.evaluate(35, 80.0, "LIC-001", "VETERAN");
            assertThat(result.eligible()).isFalse();
            assertThat(result.reason()).isEqualTo(EvaluationResult.CATEGORY_MISMATCH);
        }
    }
}
