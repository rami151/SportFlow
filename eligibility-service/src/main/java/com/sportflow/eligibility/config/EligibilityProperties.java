package com.sportflow.eligibility.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eligibility.rules")
public class EligibilityProperties {

    private int minAge = 6;
    private int maxAge = 80;
    private double minWeight = 20.0;
    private double maxWeight = 200.0;
    private int juniorMaxAge = 17;
    private int seniorMinAge = 18;
    private int seniorMaxAge = 35;
    private int veteranMinAge = 36;

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getJuniorMaxAge() {
        return juniorMaxAge;
    }

    public void setJuniorMaxAge(int juniorMaxAge) {
        this.juniorMaxAge = juniorMaxAge;
    }

    public int getSeniorMinAge() {
        return seniorMinAge;
    }

    public void setSeniorMinAge(int seniorMinAge) {
        this.seniorMinAge = seniorMinAge;
    }

    public int getSeniorMaxAge() {
        return seniorMaxAge;
    }

    public void setSeniorMaxAge(int seniorMaxAge) {
        this.seniorMaxAge = seniorMaxAge;
    }

    public int getVeteranMinAge() {
        return veteranMinAge;
    }

    public void setVeteranMinAge(int veteranMinAge) {
        this.veteranMinAge = veteranMinAge;
    }
}
