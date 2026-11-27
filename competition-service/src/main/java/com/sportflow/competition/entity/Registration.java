package com.sportflow.competition.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "competition_id", nullable = false)
    private UUID competitionId;

    @Column(name = "athlete_id", nullable = false)
    private UUID athleteId;

    @Column(name = "athlete_age", nullable = false)
    private int athleteAge;

    @Column(name = "athlete_weight", nullable = false)
    private double athleteWeight;

    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RegistrationStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Registration() {
    }

    public Registration(UUID id, UUID competitionId, UUID athleteId, int athleteAge, double athleteWeight, String licenseNumber, String category, RegistrationStatus status, String rejectionReason, Instant createdAt) {
        this.id = id;
        this.competitionId = competitionId;
        this.athleteId = athleteId;
        this.athleteAge = athleteAge;
        this.athleteWeight = athleteWeight;
        this.licenseNumber = licenseNumber;
        this.category = category;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = RegistrationStatus.PENDING;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(UUID competitionId) {
        this.competitionId = competitionId;
    }

    public UUID getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(UUID athleteId) {
        this.athleteId = athleteId;
    }

    public int getAthleteAge() {
        return athleteAge;
    }

    public void setAthleteAge(int athleteAge) {
        this.athleteAge = athleteAge;
    }

    public double getAthleteWeight() {
        return athleteWeight;
    }

    public void setAthleteWeight(double athleteWeight) {
        this.athleteWeight = athleteWeight;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
