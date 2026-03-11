package com.floodrescue.resource.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.resource.domain.entity.Warehouse;
import com.floodrescue.resource.dto.request.CreateWarehouseRequest;
import com.floodrescue.resource.dto.response.WarehouseResponse;
import com.floodrescue.resource.mapper.ReliefItemMapper;
import com.floodrescue.resource.repository.WarehouseRepository;
import com.floodrescue.resource.service.WarehouseService;
import com.floodrescue.resource.shared.exception.AppException;
import com.floodrescue.resource.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public WarehouseResponse create(CreateWarehouseRequest request, Long managerId) {
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .address(request.getAddress())
                .managerId(managerId)
                .build();
        return toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAllWithItems().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return toResponse(warehouse);
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .managerId(warehouse.getManagerId())
                .createdAt(warehouse.getCreatedAt())
                .items(warehouse.getItems().stream()
                        .map(ReliefItemMapper::toResponse)
                        .toList())
                .build();
    }
}
