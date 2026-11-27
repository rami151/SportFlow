package com.sportflow.live.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportflow.live.dto.LiveEventResponse;
import com.sportflow.live.dto.MatchEndRequest;
import com.sportflow.live.dto.MatchStartRequest;
import com.sportflow.live.dto.ScoreUpdateRequest;
import com.sportflow.live.service.LiveEventService;
import com.sportflow.live.simulator.MatchSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LiveEventController.class)
class LiveEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LiveEventService liveEventService;

    @MockBean
    private MatchSimulator matchSimulator;

    @Test
    @DisplayName("POST /api/live/{competitionId}/start returns 201 Created")
    void testStartMatch() throws Exception {
        MatchStartRequest request = new MatchStartRequest("Athlete A", "Athlete B");
        LiveEventResponse response = new LiveEventResponse(
                UUID.randomUUID(), "comp-1", "START", "Athlete A", "Athlete B",
                0, 0, 100L, Instant.now()
        );

        when(liveEventService.startMatch(eq("comp-1"), any(MatchStartRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/live/comp-1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventType").value("START"))
                .andExpect(jsonPath("$.athleteA").value("Athlete A"));
    }

    @Test
    @DisplayName("POST /api/live/{competitionId}/start with blank names returns 400")
    void testStartMatchValidationFailure() throws Exception {
        MatchStartRequest request = new MatchStartRequest("", "");

        mockMvc.perform(post("/api/live/comp-1/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/live/{competitionId}/score returns 200 OK")
    void testUpdateScore() throws Exception {
        ScoreUpdateRequest request = new ScoreUpdateRequest("Athlete A", "Athlete B", 1, 0);
        LiveEventResponse response = new LiveEventResponse(
                UUID.randomUUID(), "comp-1", "SCORE_UPDATE", "Athlete A", "Athlete B",
                1, 0, 120L, Instant.now()
        );

        when(liveEventService.updateScore(eq("comp-1"), any(ScoreUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/live/comp-1/score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("SCORE_UPDATE"))
                .andExpect(jsonPath("$.scoreA").value(1));
    }

    @Test
    @DisplayName("POST /api/live/{competitionId}/end returns 200 OK")
    void testEndMatch() throws Exception {
        MatchEndRequest request = new MatchEndRequest("Athlete A", "Athlete B", 2, 0);
        LiveEventResponse response = new LiveEventResponse(
                UUID.randomUUID(), "comp-1", "END", "Athlete A", "Athlete B",
                2, 0, 150L, Instant.now()
        );

        when(liveEventService.endMatch(eq("comp-1"), any(MatchEndRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/live/comp-1/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventType").value("END"))
                .andExpect(jsonPath("$.scoreA").value(2));
    }

    @Test
    @DisplayName("GET /api/live/{competitionId}/history returns list of events")
    void testGetHistory() throws Exception {
        LiveEventResponse response = new LiveEventResponse(
                UUID.randomUUID(), "comp-1", "START", "Athlete A", "Athlete B",
                0, 0, 100L, Instant.now()
        );

        when(liveEventService.getHistory("comp-1")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/live/comp-1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("START"));
    }

    @Test
    @DisplayName("POST /api/live/{competitionId}/simulate returns 202 Accepted")
    void testSimulate() throws Exception {
        when(matchSimulator.simulateMatchAsync(eq("comp-1"), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/live/comp-1/simulate"))
                .andExpect(status().isAccepted());
    }
}
