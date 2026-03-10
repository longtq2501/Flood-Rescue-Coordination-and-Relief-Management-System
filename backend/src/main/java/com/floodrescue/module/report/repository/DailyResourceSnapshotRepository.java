package com.floodrescue.module.report.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.report.domain.entity.DailyResourceSnapshot;

@Repository
public interface DailyResourceSnapshotRepository
        extends JpaRepository<DailyResourceSnapshot, Long> {

    Optional<DailyResourceSnapshot> findBySnapshotDateAndWarehouseId(
            LocalDate date, Long warehouseId);

    List<DailyResourceSnapshot> findBySnapshotDate(LocalDate date);

    List<DailyResourceSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(
            LocalDate from, LocalDate to);
}