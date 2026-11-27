package com.sportflow.competition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportflow.competition.dto.*;
import com.sportflow.competition.entity.CompetitionStatus;
import com.sportflow.competition.entity.RegistrationStatus;
import com.sportflow.competition.exception.CompetitionClosedException;
import com.sportflow.competition.exception.CompetitionFullException;
import com.sportflow.competition.exception.DuplicateRegistrationException;
import com.sportflow.competition.exception.ResourceNotFoundException;
import com.sportflow.competition.service.CompetitionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CompetitionController.class, RegistrationController.class})
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompetitionService competitionService;

    @Test
    @DisplayName("GET /api/competitions returns 200 and list")
    void getAllCompetitions() throws Exception {
        CompetitionResponse comp = new CompetitionResponse(
                UUID.randomUUID(), "Tunis Open", "Judo", "Tunis",
                Instant.now(), "SENIOR", 50, CompetitionStatus.OPEN, 0, Instant.now()
        );
        when(competitionService.getAllCompetitions()).thenReturn(List.of(comp));

        mockMvc.perform(get("/api/competitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tunis Open"));
    }

    @Test
    @DisplayName("POST /api/competitions creates competition and returns 201")
    void createCompetition() throws Exception {
        UUID id = UUID.randomUUID();
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "Tunis Open", "Judo", "Tunis",
                Instant.now().plusSeconds(86400), "SENIOR", 50
        );
        CompetitionResponse response = new CompetitionResponse(
                id, "Tunis Open", "Judo", "Tunis",
                request.date(), "SENIOR", 50, CompetitionStatus.OPEN, 0, Instant.now()
        );

        when(competitionService.createCompetition(any(CompetitionCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Tunis Open"));
    }

    @Test
    @DisplayName("POST /api/competitions with invalid data returns 400")
    void createCompetition_Invalid() throws Exception {
        CompetitionCreateRequest request = new CompetitionCreateRequest(
                "", "", "", null, "", 0
        );

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/competitions/{id} returns competition")
    void getCompetitionById() throws Exception {
        UUID id = UUID.randomUUID();
        CompetitionResponse response = new CompetitionResponse(
                id, "Tunis Open", "Judo", "Tunis",
                Instant.now(), "SENIOR", 50, CompetitionStatus.OPEN, 0, Instant.now()
        );

        when(competitionService.getCompetitionById(id)).thenReturn(response);

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /api/competitions/{id} returns 404 when not found")
    void getCompetitionById_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(competitionService.getCompetitionById(id))
                .thenThrow(new ResourceNotFoundException("Competition not found"));

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/competitions/{id} updates competition")
    void updateCompetition() throws Exception {
        UUID id = UUID.randomUUID();
        CompetitionUpdateRequest request = new CompetitionUpdateRequest(
                "Updated Name", "Judo", "Tunis", Instant.now().plusSeconds(86400),
                "SENIOR", 60, CompetitionStatus.CLOSED
        );
        CompetitionResponse response = new CompetitionResponse(
                id, "Updated Name", "Judo", "Tunis", request.date(),
                "SENIOR", 60, CompetitionStatus.CLOSED, 0, Instant.now()
        );

        when(competitionService.updateCompetition(eq(id), any(CompetitionUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/competitions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("DELETE /api/competitions/{id} returns 204 No Content")
    void deleteCompetition() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(competitionService).deleteCompetition(id);

        mockMvc.perform(delete("/api/competitions/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/competitions/{id}/registrations creates registration and returns 201")
    void registerAthlete() throws Exception {
        UUID competitionId = UUID.randomUUID();
        UUID athleteId = UUID.randomUUID();
        RegistrationCreateRequest request = new RegistrationCreateRequest(
                athleteId, 25, 75.0, "LIC-12345", "SENIOR"
        );
        RegistrationResponse response = new RegistrationResponse(
                UUID.randomUUID(), competitionId, athleteId, 25, 75.0, "LIC-12345",
                "SENIOR", RegistrationStatus.PENDING, null, Instant.now()
        );

        when(competitionService.registerAthlete(eq(competitionId), any(RegistrationCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/competitions/{id}/registrations", competitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.athleteId").value(athleteId.toString()));
    }

    @Test
    @DisplayName("POST /api/competitions/{id}/registrations returns 400 when competition closed")
    void registerAthlete_Closed() throws Exception {
        UUID competitionId = UUID.randomUUID();
        RegistrationCreateRequest request = new RegistrationCreateRequest(
                UUID.randomUUID(), 25, 75.0, "LIC-12345", "SENIOR"
        );

        when(competitionService.registerAthlete(eq(competitionId), any(RegistrationCreateRequest.class)))
                .thenThrow(new CompetitionClosedException("Cannot register to closed competition"));

        mockMvc.perform(post("/api/competitions/{id}/registrations", competitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot register to closed competition"));
    }

    @Test
    @DisplayName("POST /api/competitions/{id}/registrations returns 409 when full or duplicate")
    void registerAthlete_Full() throws Exception {
        UUID competitionId = UUID.randomUUID();
        RegistrationCreateRequest request = new RegistrationCreateRequest(
                UUID.randomUUID(), 25, 75.0, "LIC-12345", "SENIOR"
        );

        when(competitionService.registerAthlete(eq(competitionId), any(RegistrationCreateRequest.class)))
                .thenThrow(new CompetitionFullException("Competition is full"));

        mockMvc.perform(post("/api/competitions/{id}/registrations", competitionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/competitions/{id}/registrations returns list")
    void getRegistrationsForCompetition() throws Exception {
        UUID competitionId = UUID.randomUUID();
        RegistrationResponse reg = new RegistrationResponse(
                UUID.randomUUID(), competitionId, UUID.randomUUID(), 25, 75.0, "LIC-12345",
                "SENIOR", RegistrationStatus.PENDING, null, Instant.now()
        );

        when(competitionService.getRegistrationsForCompetition(competitionId)).thenReturn(List.of(reg));

        mockMvc.perform(get("/api/competitions/{id}/registrations", competitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].competitionId").value(competitionId.toString()));
    }

    @Test
    @DisplayName("GET /api/registrations/{id} returns registration")
    void getRegistrationById() throws Exception {
        UUID regId = UUID.randomUUID();
        RegistrationResponse reg = new RegistrationResponse(
                regId, UUID.randomUUID(), UUID.randomUUID(), 25, 75.0, "LIC-12345",
                "SENIOR", RegistrationStatus.PENDING, null, Instant.now()
        );

        when(competitionService.getRegistrationById(regId)).thenReturn(reg);

        mockMvc.perform(get("/api/registrations/{id}", regId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(regId.toString()));
    }
}
