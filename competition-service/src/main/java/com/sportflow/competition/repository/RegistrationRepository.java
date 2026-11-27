package com.sportflow.competition.repository;

import com.sportflow.competition.entity.Registration;
import com.sportflow.competition.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByCompetitionId(UUID competitionId);
    boolean existsByCompetitionIdAndAthleteId(UUID competitionId, UUID athleteId);
    long countByCompetitionId(UUID competitionId);
    long countByCompetitionIdAndStatusNot(UUID competitionId, RegistrationStatus status);
}
