package com.floodrescue.report.external.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.report.external.entity.RescueTeam;
import com.floodrescue.report.external.enums.TeamStatus;

@Repository
public interface RescueTeamRepository extends JpaRepository<RescueTeam, Long> {
    long countByStatus(TeamStatus status);
}
