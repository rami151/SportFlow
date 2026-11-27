package com.sportflow.competition.service;

import com.sportflow.competition.client.EligibilityCheckResult;
import com.sportflow.competition.client.EligibilityClient;
import com.sportflow.competition.dto.*;
import com.sportflow.competition.entity.*;
import com.sportflow.competition.exception.*;
import com.sportflow.competition.repository.CompetitionRepository;
import com.sportflow.competition.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EligibilityClient eligibilityClient;

    @InjectMocks
    private CompetitionService competitionService;

    private UUID competitionId;
    private Competition competition;

    @BeforeEach
    void setUp() {
        competitionId = UUID.randomUUID();
        competition = new Competition(
                competitionId,
                "Tunis Judo Open 2026",
                "Judo",
                "Tunis, Tunisia",
                Instant.now().plusSeconds(86400),
                "SENIOR",
                50,
                CompetitionStatus.OPEN,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("Competition CRUD Tests")
    class CrudTests {

        @Test
        @DisplayName("createCompetition saves and returns CompetitionResponse")
        void createCompetition() {
            CompetitionCreateRequest request = new CompetitionCreateRequest(
                    "Tunis Judo Open 2026", "Judo", "Tunis, Tunisia",
                    Instant.now().plusSeconds(86400), "SENIOR", 50
            );

            when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

            CompetitionResponse response = competitionService.createCompetition(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(competitionId);
            assertThat(response.name()).isEqualTo("Tunis Judo Open 2026");
            assertThat(response.status()).isEqualTo(CompetitionStatus.OPEN);
            verify(competitionRepository).save(any(Competition.class));
        }

        @Test
        @DisplayName("getCompetitionById returns competition when found")
        void getCompetitionById_Success() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED))
                    .thenReturn(5L);

            CompetitionResponse response = competitionService.getCompetitionById(competitionId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(competitionId);
            assertThat(response.currentParticipants()).isEqualTo(5L);
        }

        @Test
        @DisplayName("getCompetitionById throws ResourceNotFoundException when not found")
        void getCompetitionById_NotFound() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> competitionService.getCompetitionById(competitionId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Competition not found with id: " + competitionId);
        }

        @Test
        @DisplayName("updateCompetition modifies existing competition")
        void updateCompetition_Success() {
            CompetitionUpdateRequest updateRequest = new CompetitionUpdateRequest(
                    "Updated Name", "Karate", "Sousse",
                    Instant.now().plusSeconds(172800), "VETERAN", 30,
                    CompetitionStatus.CLOSED
            );

            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(competitionRepository.save(any(Competition.class))).thenAnswer(inv -> inv.getArgument(0));

            CompetitionResponse response = competitionService.updateCompetition(competitionId, updateRequest);

            assertThat(response.name()).isEqualTo("Updated Name");
            assertThat(response.sport()).isEqualTo("Karate");
            assertThat(response.status()).isEqualTo(CompetitionStatus.CLOSED);
        }

        @Test
        @DisplayName("deleteCompetition deletes existing competition")
        void deleteCompetition_Success() {
            when(competitionRepository.existsById(competitionId)).thenReturn(true);

            competitionService.deleteCompetition(competitionId);

            verify(competitionRepository).deleteById(competitionId);
        }

        @Test
        @DisplayName("deleteCompetition throws ResourceNotFoundException when not found")
        void deleteCompetition_NotFound() {
            when(competitionRepository.existsById(competitionId)).thenReturn(false);

            assertThatThrownBy(() -> competitionService.deleteCompetition(competitionId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registration Business Rules and Eligibility Flow")
    class RegistrationRulesTests {

        private UUID athleteId;
        private RegistrationCreateRequest regRequest;

        @BeforeEach
        void initReg() {
            athleteId = UUID.randomUUID();
            regRequest = new RegistrationCreateRequest(
                    athleteId, 25, 73.5, "LIC-12345", "SENIOR"
            );
        }

        @Test
        @DisplayName("Eligible athlete registration transitions from PENDING to ACCEPTED")
        void registerAthlete_Eligible_Accepted() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);
            when(registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED)).thenReturn(10L);

            Registration pendingReg = new Registration(
                    UUID.randomUUID(), competitionId, athleteId, 25, 73.5, "LIC-12345", "SENIOR",
                    RegistrationStatus.PENDING, null, Instant.now()
            );
            when(registrationRepository.saveAndFlush(any(Registration.class))).thenReturn(pendingReg);
            when(eligibilityClient.checkEligibility(athleteId, competitionId, 25, 73.5, "LIC-12345", "SENIOR"))
                    .thenReturn(EligibilityCheckResult.success());
            when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

            RegistrationResponse response = competitionService.registerAthlete(competitionId, regRequest);

            assertThat(response).isNotNull();
            assertThat(response.competitionId()).isEqualTo(competitionId);
            assertThat(response.athleteId()).isEqualTo(athleteId);
            assertThat(response.status()).isEqualTo(RegistrationStatus.ACCEPTED);
            assertThat(response.rejectionReason()).isNull();

            verify(registrationRepository).saveAndFlush(any(Registration.class));
            verify(eligibilityClient).checkEligibility(athleteId, competitionId, 25, 73.5, "LIC-12345", "SENIOR");
            verify(registrationRepository).save(any(Registration.class));
        }

        @Test
        @DisplayName("Ineligible athlete registration transitions from PENDING to REJECTED with reason")
        void registerAthlete_Ineligible_Rejected() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);
            when(registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED)).thenReturn(10L);

            Registration pendingReg = new Registration(
                    UUID.randomUUID(), competitionId, athleteId, 25, 73.5, "LIC-12345", "SENIOR",
                    RegistrationStatus.PENDING, null, Instant.now()
            );
            when(registrationRepository.saveAndFlush(any(Registration.class))).thenReturn(pendingReg);
            when(eligibilityClient.checkEligibility(athleteId, competitionId, 25, 73.5, "LIC-12345", "SENIOR"))
                    .thenReturn(EligibilityCheckResult.failure("WEIGHT_OUT_OF_RANGE"));
            when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

            RegistrationResponse response = competitionService.registerAthlete(competitionId, regRequest);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(RegistrationStatus.REJECTED);
            assertThat(response.rejectionReason()).isEqualTo("WEIGHT_OUT_OF_RANGE");
        }

        @Test
        @DisplayName("Eligibility service down: retains registration as PENDING and throws 503 exception")
        void registerAthlete_EligibilityDown_RetainsPending() {
            UUID regId = UUID.randomUUID();
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);
            when(registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED)).thenReturn(10L);

            Registration pendingReg = new Registration(
                    regId, competitionId, athleteId, 25, 73.5, "LIC-12345", "SENIOR",
                    RegistrationStatus.PENDING, null, Instant.now()
            );
            when(registrationRepository.saveAndFlush(any(Registration.class))).thenReturn(pendingReg);
            when(eligibilityClient.checkEligibility(athleteId, competitionId, 25, 73.5, "LIC-12345", "SENIOR"))
                    .thenThrow(new EligibilityServiceUnavailableException("Connection refused", null));

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(EligibilityServiceUnavailableException.class)
                    .hasMessageContaining("retained as PENDING")
                    .satisfies(ex -> assertThat(((EligibilityServiceUnavailableException) ex).getRegistrationId()).isEqualTo(regId));

            assertThat(pendingReg.getStatus()).isEqualTo(RegistrationStatus.PENDING);
            assertThat(pendingReg.getRejectionReason()).isEqualTo("ELIGIBILITY_SERVICE_UNAVAILABLE");
            verify(registrationRepository).save(pendingReg);
        }

        @Test
        @DisplayName("Cannot register to CLOSED competition")
        void registerAthlete_ClosedCompetition() {
            competition.setStatus(CompetitionStatus.CLOSED);
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(CompetitionClosedException.class)
                    .hasMessageContaining("CLOSED");

            verify(registrationRepository, never()).saveAndFlush(any());
            verifyNoInteractions(eligibilityClient);
        }

        @Test
        @DisplayName("Cannot register to FINISHED competition")
        void registerAthlete_FinishedCompetition() {
            competition.setStatus(CompetitionStatus.FINISHED);
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(CompetitionClosedException.class)
                    .hasMessageContaining("FINISHED");

            verify(registrationRepository, never()).saveAndFlush(any());
            verifyNoInteractions(eligibilityClient);
        }

        @Test
        @DisplayName("Cannot register duplicate athlete in same competition")
        void registerAthlete_DuplicateAthlete() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(true);

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(DuplicateRegistrationException.class)
                    .hasMessageContaining("already registered");

            verify(registrationRepository, never()).saveAndFlush(any());
            verifyNoInteractions(eligibilityClient);
        }

        @Test
        @DisplayName("Cannot register when maxParticipants capacity reached")
        void registerAthlete_MaxCapacityReached() {
            competition.setMaxParticipants(2);
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.of(competition));
            when(registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, athleteId)).thenReturn(false);
            when(registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED)).thenReturn(2L);

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(CompetitionFullException.class)
                    .hasMessageContaining("max capacity");

            verify(registrationRepository, never()).saveAndFlush(any());
            verifyNoInteractions(eligibilityClient);
        }

        @Test
        @DisplayName("Register throws ResourceNotFoundException when competition does not exist")
        void registerAthlete_CompetitionNotFound() {
            when(competitionRepository.findById(competitionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> competitionService.registerAthlete(competitionId, regRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(eligibilityClient);
        }

        @Test
        @DisplayName("getRegistrationById returns registration when found")
        void getRegistrationById_Success() {
            UUID regId = UUID.randomUUID();
            Registration reg = new Registration(
                    regId, competitionId, athleteId, 25, 73.5, "LIC-12345", "SENIOR",
                    RegistrationStatus.ACCEPTED, null, Instant.now()
            );
            when(registrationRepository.findById(regId)).thenReturn(Optional.of(reg));

            RegistrationResponse response = competitionService.getRegistrationById(regId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(regId);
        }

        @Test
        @DisplayName("getRegistrationsForCompetition returns list for existing competition")
        void getRegistrationsForCompetition_Success() {
            when(competitionRepository.existsById(competitionId)).thenReturn(true);
            when(registrationRepository.findByCompetitionId(competitionId)).thenReturn(List.of(
                    new Registration(UUID.randomUUID(), competitionId, athleteId, 25, 73.5, "LIC-1", "SENIOR", RegistrationStatus.ACCEPTED, null, Instant.now())
            ));

            List<RegistrationResponse> list = competitionService.getRegistrationsForCompetition(competitionId);
            assertThat(list).hasSize(1);
        }
    }
}
