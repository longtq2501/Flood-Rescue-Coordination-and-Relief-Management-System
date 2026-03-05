package com.floodrescue.module.resource.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.floodrescue.module.resource.domain.entity.Vehicle;
import com.floodrescue.module.resource.domain.enums.VehicleStatus;
import com.floodrescue.module.resource.domain.enums.VehicleType;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByStatusAndType(VehicleStatus status, VehicleType type);

    boolean existsByPlateNumber(String plateNumber);
}