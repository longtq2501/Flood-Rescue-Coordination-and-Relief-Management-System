package com.floodrescue.module.resource.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.resource.domain.entity.Vehicle;
import com.floodrescue.module.resource.domain.entity.VehicleLog;
import com.floodrescue.module.resource.domain.enums.VehicleLogAction;
import com.floodrescue.module.resource.domain.enums.VehicleStatus;
import com.floodrescue.module.resource.domain.enums.VehicleType;
import com.floodrescue.module.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.module.resource.dto.response.VehicleResponse;
import com.floodrescue.module.resource.repository.VehicleLogRepository;
import com.floodrescue.module.resource.repository.VehicleRepository;
import com.floodrescue.module.resource.service.VehicleService;
import com.floodrescue.shared.exception.AppException;
import com.floodrescue.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleLogRepository vehicleLogRepository;

    @Override
    @Transactional(value = "resourceTransactionManager")
    public VehicleResponse create(CreateVehicleRequest request) {
        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new AppException(ErrorCode.DUPLICATE_PLATE_NUMBER);
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.getPlateNumber())
                .type(request.getType())
                .capacity(request.getCapacity())
                .build();

        return toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(value = "resourceTransactionManager", readOnly = true)
    public List<VehicleResponse> getAll(VehicleStatus status, VehicleType type) {
        List<Vehicle> vehicles;
        if (status != null && type != null) {
            vehicles = vehicleRepository.findByStatusAndType(status, type);
        } else if (status != null) {
            vehicles = vehicleRepository.findByStatus(status);
        } else if (type != null) {
            vehicles = vehicleRepository.findByType(type);
        } else {
            vehicles = vehicleRepository.findAll();
        }

        return vehicles.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(value = "resourceTransactionManager", readOnly = true)
    public VehicleResponse getById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    @Override
    @Transactional(value = "resourceTransactionManager")
    public VehicleResponse updateStatus(Long vehicleId, VehicleStatus status, String note) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        vehicle.setStatus(status);
        Vehicle saved = vehicleRepository.save(vehicle);

        vehicleLogRepository.save(VehicleLog.builder()
                .vehicle(saved)
                .action(mapStatusToAction(status))
                .note(note)
                .build());

        return toResponse(saved);
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .type(vehicle.getType())
                .capacity(vehicle.getCapacity())
                .status(vehicle.getStatus())
                .currentLat(vehicle.getCurrentLat())
                .currentLng(vehicle.getCurrentLng())
                .assignedTeamId(vehicle.getAssignedTeamId())
                .build();
    }

    private VehicleLogAction mapStatusToAction(VehicleStatus status) {
        return switch (status) {
            case AVAILABLE -> VehicleLogAction.RELEASED;
            case IN_USE -> VehicleLogAction.ASSIGNED;
            case MAINTENANCE -> VehicleLogAction.MAINTENANCE;
            case OFFLINE -> VehicleLogAction.OFFLINE;
        };
    }
}
