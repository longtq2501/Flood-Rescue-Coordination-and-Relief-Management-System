package com.floodrescue.resource.service;

import java.util.List;

import com.floodrescue.resource.dto.request.CreateWarehouseRequest;
import com.floodrescue.resource.dto.response.WarehouseResponse;

public interface WarehouseService {
    WarehouseResponse create(CreateWarehouseRequest request, Long managerId);
    List<WarehouseResponse> getAll();
    WarehouseResponse getById(Long warehouseId);
}
