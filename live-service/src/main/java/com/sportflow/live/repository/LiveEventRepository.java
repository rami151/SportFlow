package com.sportflow.live.repository;

import com.sportflow.live.entity.LiveEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LiveEventRepository extends JpaRepository<LiveEvent, UUID> {
    List<LiveEvent> findByCompetitionIdOrderByEventTimestampAsc(String competitionId);
}
