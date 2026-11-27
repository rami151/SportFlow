package com.sportflow.live.service;

import com.sportflow.live.dto.LiveEventResponse;
import com.sportflow.live.dto.MatchEndRequest;
import com.sportflow.live.dto.MatchStartRequest;
import com.sportflow.live.dto.ScoreUpdateRequest;
import com.sportflow.live.entity.LiveEvent;
import com.sportflow.live.grpc.EventType;
import com.sportflow.live.registry.LiveStreamRegistry;
import com.sportflow.live.repository.LiveEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveEventServiceTest {

    @Mock
    private LiveEventRepository eventRepository;

    @Mock
    private LiveStreamRegistry streamRegistry;

    @InjectMocks
    private LiveEventService liveEventService;

    private String competitionId;

    @BeforeEach
    void setUp() {
        competitionId = "comp-555";
    }

    @Test
    @DisplayName("startMatch saves event and broadcasts proto to subscribers")
    void testStartMatch() {
        MatchStartRequest request = new MatchStartRequest("Athlete A", "Athlete B");
        when(eventRepository.save(any(LiveEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        LiveEventResponse response = liveEventService.startMatch(competitionId, request);

        assertThat(response).isNotNull();
        assertThat(response.competitionId()).isEqualTo(competitionId);
        assertThat(response.eventType()).isEqualTo(EventType.START.name());
        assertThat(response.athleteA()).isEqualTo("Athlete A");
        assertThat(response.athleteB()).isEqualTo("Athlete B");
        assertThat(response.scoreA()).isEqualTo(0);
        assertThat(response.scoreB()).isEqualTo(0);

        verify(eventRepository).save(any(LiveEvent.class));
        verify(streamRegistry).broadcast(eq(competitionId), any(com.sportflow.live.grpc.LiveEvent.class));
    }

    @Test
    @DisplayName("updateScore saves SCORE_UPDATE event and broadcasts")
    void testUpdateScore() {
        ScoreUpdateRequest request = new ScoreUpdateRequest("Athlete A", "Athlete B", 2, 1);
        when(eventRepository.save(any(LiveEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        LiveEventResponse response = liveEventService.updateScore(competitionId, request);

        assertThat(response.eventType()).isEqualTo(EventType.SCORE_UPDATE.name());
        assertThat(response.scoreA()).isEqualTo(2);
        assertThat(response.scoreB()).isEqualTo(1);

        verify(eventRepository).save(any(LiveEvent.class));
        verify(streamRegistry).broadcast(eq(competitionId), any(com.sportflow.live.grpc.LiveEvent.class));
    }

    @Test
    @DisplayName("endMatch saves END event and broadcasts")
    void testEndMatch() {
        MatchEndRequest request = new MatchEndRequest("Athlete A", "Athlete B", 3, 1);
        when(eventRepository.save(any(LiveEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        LiveEventResponse response = liveEventService.endMatch(competitionId, request);

        assertThat(response.eventType()).isEqualTo(EventType.END.name());
        assertThat(response.scoreA()).isEqualTo(3);
        assertThat(response.scoreB()).isEqualTo(1);

        verify(eventRepository).save(any(LiveEvent.class));
        verify(streamRegistry).broadcast(eq(competitionId), any(com.sportflow.live.grpc.LiveEvent.class));
    }

    @Test
    @DisplayName("getHistory returns list of events in chronological order")
    void testGetHistory() {
        LiveEvent event1 = new LiveEvent(UUID.randomUUID(), competitionId, "START", "A", "B", 0, 0, 100L, Instant.now());
        LiveEvent event2 = new LiveEvent(UUID.randomUUID(), competitionId, "END", "A", "B", 1, 0, 200L, Instant.now());
        when(eventRepository.findByCompetitionIdOrderByEventTimestampAsc(competitionId))
                .thenReturn(List.of(event1, event2));

        List<LiveEventResponse> history = liveEventService.getHistory(competitionId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).eventType()).isEqualTo("START");
        assertThat(history.get(1).eventType()).isEqualTo("END");
    }
}
