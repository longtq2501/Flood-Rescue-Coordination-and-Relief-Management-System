package com.floodrescue.module.report.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.report.domain.entity.DailyRequestSnapshot;

@Repository
public interface DailyRequestSnapshotRepository
        extends JpaRepository<DailyRequestSnapshot, Long> {

    Optional<DailyRequestSnapshot> findBySnapshotDate(LocalDate date);

    // Lấy 30 ngày gần nhất cho chart
    List<DailyRequestSnapshot> findTop30ByOrderBySnapshotDateDesc();

    List<DailyRequestSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(
            LocalDate from, LocalDate to);
}