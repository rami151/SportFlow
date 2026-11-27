package com.sportflow.live.service;

import com.sportflow.live.dto.*;
import com.sportflow.live.entity.LiveEvent;
import com.sportflow.live.grpc.EventType;
import com.sportflow.live.registry.LiveStreamRegistry;
import com.sportflow.live.repository.LiveEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LiveEventService {

    private static final Logger log = LoggerFactory.getLogger(LiveEventService.class);

    private final LiveEventRepository eventRepository;
    private final LiveStreamRegistry streamRegistry;

    public LiveEventService(LiveEventRepository eventRepository, LiveStreamRegistry streamRegistry) {
        this.eventRepository = eventRepository;
        this.streamRegistry = streamRegistry;
    }

    public LiveEventResponse startMatch(String competitionId, MatchStartRequest request) {
        log.info("Starting match in competition {}: {} vs {}", competitionId, request.athleteA(), request.athleteB());

        LiveEvent event = new LiveEvent(
                UUID.randomUUID(),
                competitionId,
                EventType.START.name(),
                request.athleteA(),
                request.athleteB(),
                0,
                0,
                Instant.now().getEpochSecond(),
                Instant.now()
        );

        LiveEvent saved = eventRepository.save(event);
        broadcastProto(saved);
        return toResponse(saved);
    }

    public LiveEventResponse updateScore(String competitionId, ScoreUpdateRequest request) {
        log.info("Updating score in competition {}: {} ({}) - ({}) {}",
                competitionId, request.athleteA(), request.scoreA(), request.scoreB(), request.athleteB());

        LiveEvent event = new LiveEvent(
                UUID.randomUUID(),
                competitionId,
                EventType.SCORE_UPDATE.name(),
                request.athleteA(),
                request.athleteB(),
                request.scoreA(),
                request.scoreB(),
                Instant.now().getEpochSecond(),
                Instant.now()
        );

        LiveEvent saved = eventRepository.save(event);
        broadcastProto(saved);
        return toResponse(saved);
    }

    public LiveEventResponse endMatch(String competitionId, MatchEndRequest request) {
        log.info("Ending match in competition {}: final score {} ({}) - ({}) {}",
                competitionId, request.athleteA(), request.scoreA(), request.scoreB(), request.athleteB());

        LiveEvent event = new LiveEvent(
                UUID.randomUUID(),
                competitionId,
                EventType.END.name(),
                request.athleteA(),
                request.athleteB(),
                request.scoreA(),
                request.scoreB(),
                Instant.now().getEpochSecond(),
                Instant.now()
        );

        LiveEvent saved = eventRepository.save(event);
        broadcastProto(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LiveEventResponse> getHistory(String competitionId) {
        return eventRepository.findByCompetitionIdOrderByEventTimestampAsc(competitionId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void broadcastProto(LiveEvent entity) {
        com.sportflow.live.grpc.LiveEvent proto = com.sportflow.live.grpc.LiveEvent.newBuilder()
                .setCompetitionId(entity.getCompetitionId())
                .setType(EventType.valueOf(entity.getEventType()))
                .setAthleteA(entity.getAthleteA())
                .setAthleteB(entity.getAthleteB())
                .setScoreA(entity.getScoreA())
                .setScoreB(entity.getScoreB())
                .setTimestamp(entity.getEventTimestamp())
                .build();

        streamRegistry.broadcast(entity.getCompetitionId(), proto);
    }

    private LiveEventResponse toResponse(LiveEvent entity) {
        return new LiveEventResponse(
                entity.getId(),
                entity.getCompetitionId(),
                entity.getEventType(),
                entity.getAthleteA(),
                entity.getAthleteB(),
                entity.getScoreA(),
                entity.getScoreB(),
                entity.getEventTimestamp(),
                entity.getCreatedAt()
        );
    }
}
