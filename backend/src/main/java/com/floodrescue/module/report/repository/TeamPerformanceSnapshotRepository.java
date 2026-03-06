package com.floodrescue.module.report.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.report.domain.entity.TeamPerformanceSnapshot;

@Repository
public interface TeamPerformanceSnapshotRepository
        extends JpaRepository<TeamPerformanceSnapshot, Long> {

    Optional<TeamPerformanceSnapshot> findBySnapshotDateAndTeamId(
            LocalDate date, Long teamId);

    List<TeamPerformanceSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(
            LocalDate from, LocalDate to);

    // Top team hoàn thành nhiều nhiệm vụ nhất trong khoảng thời gian
    List<TeamPerformanceSnapshot> findBySnapshotDateBetweenOrderByMissionsCompletedDesc(
            LocalDate from, LocalDate to);
}