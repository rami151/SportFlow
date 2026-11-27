package com.sportflow.live.controller;

import com.sportflow.live.dto.*;
import com.sportflow.live.service.LiveEventService;
import com.sportflow.live.simulator.MatchSimulator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live")
public class LiveEventController {

    private final LiveEventService liveEventService;
    private final MatchSimulator matchSimulator;

    public LiveEventController(LiveEventService liveEventService, MatchSimulator matchSimulator) {
        this.liveEventService = liveEventService;
        this.matchSimulator = matchSimulator;
    }

    @PostMapping("/{competitionId}/start")
    public ResponseEntity<LiveEventResponse> startMatch(@PathVariable String competitionId,
                                                        @Valid @RequestBody MatchStartRequest request) {
        LiveEventResponse response = liveEventService.startMatch(competitionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{competitionId}/score")
    public ResponseEntity<LiveEventResponse> updateScore(@PathVariable String competitionId,
                                                         @Valid @RequestBody ScoreUpdateRequest request) {
        LiveEventResponse response = liveEventService.updateScore(competitionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{competitionId}/end")
    public ResponseEntity<LiveEventResponse> endMatch(@PathVariable String competitionId,
                                                      @Valid @RequestBody MatchEndRequest request) {
        LiveEventResponse response = liveEventService.endMatch(competitionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{competitionId}/history")
    public ResponseEntity<List<LiveEventResponse>> getHistory(@PathVariable String competitionId) {
        return ResponseEntity.ok(liveEventService.getHistory(competitionId));
    }

    @PostMapping("/{competitionId}/simulate")
    public ResponseEntity<String> simulateMatch(@PathVariable String competitionId,
                                                @RequestParam(defaultValue = "Athlete A") String athleteA,
                                                @RequestParam(defaultValue = "Athlete B") String athleteB) {
        matchSimulator.simulateMatchAsync(competitionId, athleteA, athleteB);
        return ResponseEntity.accepted().body("Match simulation started for competition: " + competitionId);
    }
}
