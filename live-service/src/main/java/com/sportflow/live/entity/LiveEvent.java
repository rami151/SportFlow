package com.sportflow.live.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_events")
public class LiveEvent {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "competition_id", nullable = false)
    private String competitionId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "athlete_a", nullable = false)
    private String athleteA;

    @Column(name = "athlete_b", nullable = false)
    private String athleteB;

    @Column(name = "score_a", nullable = false)
    private int scoreA;

    @Column(name = "score_b", nullable = false)
    private int scoreB;

    @Column(name = "event_timestamp", nullable = false)
    private long eventTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LiveEvent() {
    }

    public LiveEvent(UUID id, String competitionId, String eventType, String athleteA, String athleteB, int scoreA, int scoreB, long eventTimestamp, Instant createdAt) {
        this.id = id;
        this.competitionId = competitionId;
        this.eventType = eventType;
        this.athleteA = athleteA;
        this.athleteB = athleteB;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.eventTimestamp = eventTimestamp;
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
        if (this.eventTimestamp == 0) {
            this.eventTimestamp = Instant.now().getEpochSecond();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAthleteA() {
        return athleteA;
    }

    public void setAthleteA(String athleteA) {
        this.athleteA = athleteA;
    }

    public String getAthleteB() {
        return athleteB;
    }

    public void setAthleteB(String athleteB) {
        this.athleteB = athleteB;
    }

    public int getScoreA() {
        return scoreA;
    }

    public void setScoreA(int scoreA) {
        this.scoreA = scoreA;
    }

    public int getScoreB() {
        return scoreB;
    }

    public void setScoreB(int scoreB) {
        this.scoreB = scoreB;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
