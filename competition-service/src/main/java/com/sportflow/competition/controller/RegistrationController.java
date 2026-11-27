package com.sportflow.competition.controller;

import com.sportflow.competition.dto.RegistrationResponse;
import com.sportflow.competition.service.CompetitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final CompetitionService competitionService;

    public RegistrationController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponse> getRegistrationById(@PathVariable UUID id) {
        return ResponseEntity.ok(competitionService.getRegistrationById(id));
    }
}
