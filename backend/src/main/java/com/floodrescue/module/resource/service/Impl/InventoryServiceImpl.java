package com.floodrescue.module.resource.service.Impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.resource.domain.entity.ReliefItem;
import com.floodrescue.module.resource.domain.entity.Warehouse;
import com.floodrescue.module.resource.dto.request.CreateReliefItemRequest;
import com.floodrescue.module.resource.dto.request.UpdateStockRequest;
import com.floodrescue.module.resource.dto.response.ReliefItemResponse;
import com.floodrescue.module.resource.repository.ReliefItemRepository;
import com.floodrescue.module.resource.repository.WarehouseRepository;
import com.floodrescue.module.resource.service.InventoryService;
import com.floodrescue.module.resource.service.mapper.ReliefItemMapper;
import com.floodrescue.shared.exception.AppException;
import com.floodrescue.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ReliefItemRepository reliefItemRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public ReliefItemResponse addItem(CreateReliefItemRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));

        ReliefItem item = ReliefItem.builder()
                .warehouse(warehouse)
                .name(request.getName())
                .category(request.getCategory())
                .unit(request.getUnit())
                .quantity(request.getQuantity())
                .lowThreshold(request.getLowThreshold())
                .build();

        return ReliefItemMapper.toResponse(reliefItemRepository.save(item));
    }

    @Override
    public Page<ReliefItemResponse> getItemsByWarehouse(Long warehouseId, Pageable pageable) {
        return reliefItemRepository.findByWarehouseId(warehouseId, pageable)
                .map(ReliefItemMapper::toResponse);
    }

    @Override
    @Transactional
    public ReliefItemResponse updateStock(Long itemId, UpdateStockRequest request) {
        ReliefItem item = reliefItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        int newQuantity = item.getQuantity() + request.getQuantity();
        if (newQuantity < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                    "Không đủ tồn kho: " + item.getName());
        }

        item.setQuantity(newQuantity);
        return ReliefItemMapper.toResponse(reliefItemRepository.save(item));
    }

    @Override
    public ReliefItemResponse getById(Long itemId) {
        return reliefItemRepository.findById(itemId)
                .map(ReliefItemMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
    }

}
