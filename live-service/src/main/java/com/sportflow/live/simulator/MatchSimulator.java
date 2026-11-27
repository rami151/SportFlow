package com.sportflow.live.simulator;

import com.sportflow.live.dto.MatchEndRequest;
import com.sportflow.live.dto.MatchStartRequest;
import com.sportflow.live.dto.ScoreUpdateRequest;
import com.sportflow.live.service.LiveEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class MatchSimulator {

    private static final Logger log = LoggerFactory.getLogger(MatchSimulator.class);

    private final LiveEventService liveEventService;

    public MatchSimulator(LiveEventService liveEventService) {
        this.liveEventService = liveEventService;
    }

    public CompletableFuture<Void> simulateMatchAsync(String competitionId, String athleteA, String athleteB) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.info("[SIMULATOR] Starting match for competition: {} ({} vs {})", competitionId, athleteA, athleteB);
                liveEventService.startMatch(competitionId, new MatchStartRequest(athleteA, athleteB));

                TimeUnit.SECONDS.sleep(2);
                log.info("[SIMULATOR] Score update: 1-0");
                liveEventService.updateScore(competitionId, new ScoreUpdateRequest(athleteA, athleteB, 1, 0));

                TimeUnit.SECONDS.sleep(2);
                log.info("[SIMULATOR] Score update: 1-1");
                liveEventService.updateScore(competitionId, new ScoreUpdateRequest(athleteA, athleteB, 1, 1));

                TimeUnit.SECONDS.sleep(2);
                log.info("[SIMULATOR] Score update: 2-1");
                liveEventService.updateScore(competitionId, new ScoreUpdateRequest(athleteA, athleteB, 2, 1));

                TimeUnit.SECONDS.sleep(2);
                log.info("[SIMULATOR] Ending match: final 2-1");
                liveEventService.endMatch(competitionId, new MatchEndRequest(athleteA, athleteB, 2, 1));

                log.info("[SIMULATOR] Match completed for competition: {}", competitionId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[SIMULATOR] Simulation interrupted for competition: {}", competitionId);
            }
        });
    }

    @Component
    @Profile("simulator")
    static class AutoRunner implements CommandLineRunner {
        private final MatchSimulator simulator;

        public AutoRunner(MatchSimulator simulator) {
            this.simulator = simulator;
        }

        @Override
        public void run(String... args) {
            log.info("[SIMULATOR-PROFILE] Auto-running simulation match on startup...");
            simulator.simulateMatchAsync("sim-comp-001", "Mohamed Gammoudi", "Hicham El Guerrouj");
        }
    }
}
