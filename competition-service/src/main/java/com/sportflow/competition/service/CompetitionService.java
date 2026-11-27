package com.sportflow.competition.service;

import com.sportflow.competition.dto.*;
import com.sportflow.competition.entity.*;
import com.sportflow.competition.exception.*;
import com.sportflow.competition.repository.CompetitionRepository;
import com.sportflow.competition.repository.RegistrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CompetitionService {

    private static final Logger log = LoggerFactory.getLogger(CompetitionService.class);

    private final CompetitionRepository competitionRepository;
    private final RegistrationRepository registrationRepository;

    public CompetitionService(CompetitionRepository competitionRepository,
                              RegistrationRepository registrationRepository) {
        this.competitionRepository = competitionRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public List<CompetitionResponse> getAllCompetitions() {
        return competitionRepository.findAll().stream()
                .map(this::toCompetitionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompetitionResponse getCompetitionById(UUID id) {
        Competition comp = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found with id: " + id));
        return toCompetitionResponse(comp);
    }

    public CompetitionResponse createCompetition(CompetitionCreateRequest request) {
        Competition comp = new Competition();
        comp.setName(request.name());
        comp.setSport(request.sport());
        comp.setLocation(request.location());
        comp.setDate(request.date());
        comp.setCategory(request.category());
        comp.setMaxParticipants(request.maxParticipants());
        comp.setStatus(CompetitionStatus.OPEN);
        comp.setCreatedAt(Instant.now());

        Competition saved = competitionRepository.save(comp);
        log.info("Created competition '{}' with id {}", saved.getName(), saved.getId());
        return toCompetitionResponse(saved);
    }

    public CompetitionResponse updateCompetition(UUID id, CompetitionUpdateRequest request) {
        Competition comp = competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found with id: " + id));

        comp.setName(request.name());
        comp.setSport(request.sport());
        comp.setLocation(request.location());
        comp.setDate(request.date());
        comp.setCategory(request.category());
        comp.setMaxParticipants(request.maxParticipants());
        comp.setStatus(request.status());

        Competition updated = competitionRepository.save(comp);
        log.info("Updated competition '{}' with id {}", updated.getName(), updated.getId());
        return toCompetitionResponse(updated);
    }

    public void deleteCompetition(UUID id) {
        if (!competitionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Competition not found with id: " + id);
        }
        competitionRepository.deleteById(id);
        log.info("Deleted competition with id {}", id);
    }

    public RegistrationResponse registerAthlete(UUID competitionId, RegistrationCreateRequest request) {
        log.info("Registering athlete {} for competition {}", request.athleteId(), competitionId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found with id: " + competitionId));

        // Rule 1: Cannot register to a CLOSED or FINISHED competition
        if (competition.getStatus() != CompetitionStatus.OPEN) {
            throw new CompetitionClosedException("Cannot register to competition in status: " + competition.getStatus());
        }

        // Rule 2: No duplicate registration of the same athlete in the same competition
        if (registrationRepository.existsByCompetitionIdAndAthleteId(competitionId, request.athleteId())) {
            throw new DuplicateRegistrationException("Athlete " + request.athleteId() + " is already registered for this competition");
        }

        // Rule 3: Respect maxParticipants (considering accepted or pending participants)
        long currentCount = registrationRepository.countByCompetitionIdAndStatusNot(competitionId, RegistrationStatus.REJECTED);
        if (currentCount >= competition.getMaxParticipants()) {
            throw new CompetitionFullException("Competition has reached max capacity of " + competition.getMaxParticipants());
        }

        // Save registration as PENDING
        Registration registration = new Registration();
        registration.setCompetitionId(competitionId);
        registration.setAthleteId(request.athleteId());
        registration.setAthleteAge(request.athleteAge());
        registration.setAthleteWeight(request.athleteWeight());
        registration.setLicenseNumber(request.licenseNumber());
        registration.setCategory(request.category());
        registration.setStatus(RegistrationStatus.PENDING);
        registration.setRejectionReason(null);
        registration.setCreatedAt(Instant.now());

        Registration saved = registrationRepository.save(registration);
        log.info("Athlete {} registered with pending status, registrationId: {}", request.athleteId(), saved.getId());
        return toRegistrationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsForCompetition(UUID competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new ResourceNotFoundException("Competition not found with id: " + competitionId);
        }
        return registrationRepository.findByCompetitionId(competitionId).stream()
                .map(this::toRegistrationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(UUID id) {
        Registration reg = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + id));
        return toRegistrationResponse(reg);
    }

    private CompetitionResponse toCompetitionResponse(Competition comp) {
        long currentParticipants = registrationRepository.countByCompetitionIdAndStatusNot(comp.getId(), RegistrationStatus.REJECTED);
        return new CompetitionResponse(
                comp.getId(),
                comp.getName(),
                comp.getSport(),
                comp.getLocation(),
                comp.getDate(),
                comp.getCategory(),
                comp.getMaxParticipants(),
                comp.getStatus(),
                currentParticipants,
                comp.getCreatedAt()
        );
    }

    private RegistrationResponse toRegistrationResponse(Registration reg) {
        return new RegistrationResponse(
                reg.getId(),
                reg.getCompetitionId(),
                reg.getAthleteId(),
                reg.getAthleteAge(),
                reg.getAthleteWeight(),
                reg.getLicenseNumber(),
                reg.getCategory(),
                reg.getStatus(),
                reg.getRejectionReason(),
                reg.getCreatedAt()
        );
    }
}
