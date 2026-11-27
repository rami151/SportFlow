package com.sportflow.notificationstats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationStatsController {

    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        // Baseline skeleton response; persistence will be wired in task B8
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        // Baseline skeleton response; aggregate statistics will be wired in task B8
        return ResponseEntity.ok(Map.of(
                "totalRegistrations", 0,
                "totalCompetitions", 0,
                "totalMatches", 0
        ));
    }
}
