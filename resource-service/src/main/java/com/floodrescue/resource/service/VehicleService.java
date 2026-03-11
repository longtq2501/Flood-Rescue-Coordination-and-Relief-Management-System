package com.floodrescue.resource.service;

import java.util.List;

import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;
import com.floodrescue.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.resource.dto.response.VehicleResponse;

public interface VehicleService {
    VehicleResponse create(CreateVehicleRequest request);
    List<VehicleResponse> getAll(VehicleStatus status, VehicleType type);
    VehicleResponse getById(Long vehicleId);
    VehicleResponse updateStatus(Long vehicleId, VehicleStatus status, String note);
}
