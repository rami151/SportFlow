package com.sportflow.competition.repository;

import com.sportflow.competition.entity.Competition;
import com.sportflow.competition.entity.CompetitionStatus;
import com.sportflow.competition.entity.Registration;
import com.sportflow.competition.entity.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompetitionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_competitions_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Test
    @DisplayName("Save and query competition in PostgreSQL via Testcontainers")
    void testSaveAndFindCompetition() {
        UUID id = UUID.randomUUID();
        Competition comp = new Competition(
                id, "Sfax Judo Cup", "Judo", "Sfax",
                Instant.now().plusSeconds(86400), "SENIOR", 20,
                CompetitionStatus.OPEN, Instant.now()
        );

        competitionRepository.save(comp);

        var found = competitionRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Sfax Judo Cup");
    }

    @Test
    @DisplayName("Save and query registrations for competition")
    void testSaveAndFindRegistration() {
        UUID compId = UUID.randomUUID();
        Competition comp = new Competition(
                compId, "Bizerte Boxing", "Boxing", "Bizerte",
                Instant.now().plusSeconds(86400), "SENIOR", 10,
                CompetitionStatus.OPEN, Instant.now()
        );
        competitionRepository.save(comp);

        UUID athleteId = UUID.randomUUID();
        Registration reg = new Registration(
                UUID.randomUUID(), compId, athleteId, 22, 67.5,
                "LIC-999", "SENIOR", RegistrationStatus.PENDING, null, Instant.now()
        );
        registrationRepository.save(reg);

        List<Registration> list = registrationRepository.findByCompetitionId(compId);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getLicenseNumber()).isEqualTo("LIC-999");

        boolean exists = registrationRepository.existsByCompetitionIdAndAthleteId(compId, athleteId);
        assertThat(exists).isTrue();

        long count = registrationRepository.countByCompetitionIdAndStatusNot(compId, RegistrationStatus.REJECTED);
        assertThat(count).isEqualTo(1L);
    }
}
