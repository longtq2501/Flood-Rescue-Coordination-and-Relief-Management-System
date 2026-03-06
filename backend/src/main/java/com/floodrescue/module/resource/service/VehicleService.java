package com.floodrescue.module.resource.service;

import java.util.List;

import com.floodrescue.module.resource.domain.enums.VehicleStatus;
import com.floodrescue.module.resource.domain.enums.VehicleType;
import com.floodrescue.module.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.module.resource.dto.response.VehicleResponse;

public interface VehicleService {

    VehicleResponse create(CreateVehicleRequest request);

    List<VehicleResponse> getAll(VehicleStatus status, VehicleType type);

    VehicleResponse getById(Long vehicleId);

    VehicleResponse updateStatus(Long vehicleId, VehicleStatus status, String note);
}