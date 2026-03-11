package com.floodrescue.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.resource.domain.entity.VehicleLog;

@Repository
public interface VehicleLogRepository extends JpaRepository<VehicleLog, Long> {
}
