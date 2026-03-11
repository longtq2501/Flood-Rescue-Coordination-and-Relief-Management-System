package com.floodrescue.dispatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.floodrescue.dispatch.domain.entity.LocationLog;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {

    @Query("SELECT l FROM LocationLog l WHERE l.team.id = :teamId ORDER BY l.loggedAt DESC LIMIT 1")
    Optional<LocationLog> findLatestByTeamId(Long teamId);

    List<LocationLog> findTop50ByTeamIdOrderByLoggedAtDesc(Long teamId);
}
