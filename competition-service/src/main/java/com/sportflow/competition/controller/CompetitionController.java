package com.sportflow.competition.controller;

import com.sportflow.competition.dto.*;
import com.sportflow.competition.service.CompetitionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/competitions")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping
    public ResponseEntity<List<CompetitionResponse>> getAllCompetitions() {
        return ResponseEntity.ok(competitionService.getAllCompetitions());
    }

    @PostMapping
    public ResponseEntity<CompetitionResponse> createCompetition(@Valid @RequestBody CompetitionCreateRequest request) {
        CompetitionResponse response = competitionService.createCompetition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getCompetitionById(@PathVariable UUID id) {
        return ResponseEntity.ok(competitionService.getCompetitionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetitionResponse> updateCompetition(@PathVariable UUID id,
                                                                 @Valid @RequestBody CompetitionUpdateRequest request) {
        return ResponseEntity.ok(competitionService.updateCompetition(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetition(@PathVariable UUID id) {
        competitionService.deleteCompetition(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/registrations")
    public ResponseEntity<RegistrationResponse> registerAthlete(@PathVariable("id") UUID competitionId,
                                                                @Valid @RequestBody RegistrationCreateRequest request) {
        RegistrationResponse response = competitionService.registerAthlete(competitionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<List<RegistrationResponse>> getRegistrationsForCompetition(@PathVariable("id") UUID competitionId) {
        return ResponseEntity.ok(competitionService.getRegistrationsForCompetition(competitionId));
    }
}
