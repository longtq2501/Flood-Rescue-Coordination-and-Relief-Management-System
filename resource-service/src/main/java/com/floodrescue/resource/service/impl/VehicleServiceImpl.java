package com.floodrescue.resource.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.resource.domain.entity.Vehicle;
import com.floodrescue.resource.domain.entity.VehicleLog;
import com.floodrescue.resource.domain.enums.VehicleLogAction;
import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;
import com.floodrescue.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.resource.dto.response.VehicleResponse;
import com.floodrescue.resource.repository.VehicleLogRepository;
import com.floodrescue.resource.repository.VehicleRepository;
import com.floodrescue.resource.service.VehicleService;
import com.floodrescue.resource.shared.exception.AppException;
import com.floodrescue.resource.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleLogRepository vehicleLogRepository;

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public VehicleResponse getById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    @Override
    @Transactional
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
