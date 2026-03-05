package com.floodrescue.module.resource.service;

import java.util.List;

import com.floodrescue.module.resource.dto.request.CreateWarehouseRequest;
import com.floodrescue.module.resource.dto.response.WarehouseResponse;

public interface WarehouseService {

    WarehouseResponse create(CreateWarehouseRequest request, Long managerId);

    List<WarehouseResponse> getAll();

    WarehouseResponse getById(Long warehouseId);
}