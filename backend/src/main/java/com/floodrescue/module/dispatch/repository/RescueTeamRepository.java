package com.floodrescue.module.dispatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.dispatch.domain.entity.RescueTeam;
import com.floodrescue.module.dispatch.domain.enums.TeamStatus;

@Repository
public interface RescueTeamRepository extends JpaRepository<RescueTeam, Long> {

    List<RescueTeam> findByStatus(TeamStatus status);

    long countByStatus(TeamStatus status);

    List<RescueTeam> findByStatusIn(List<TeamStatus> statuses);

    @Query("SELECT t FROM RescueTeam t LEFT JOIN FETCH t.members WHERE t.id = :id")
    Optional<RescueTeam> findByIdWithMembers(Long id);

    // Tìm team mà userId là leader
    Optional<RescueTeam> findByLeaderId(Long leaderId);

    // Tìm team mà userId là member
    @Query("""
            SELECT t FROM RescueTeam t
            JOIN t.members m
            WHERE m.userId = :userId
            """)
    Optional<RescueTeam> findByMemberUserId(Long userId);
}