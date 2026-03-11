package com.floodrescue.resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.resource.dto.request.CreateReliefItemRequest;
import com.floodrescue.resource.dto.request.UpdateStockRequest;
import com.floodrescue.resource.dto.response.ReliefItemResponse;

public interface InventoryService {
    ReliefItemResponse addItem(CreateReliefItemRequest request);
    Page<ReliefItemResponse> getItemsByWarehouse(Long warehouseId, Pageable pageable);
    ReliefItemResponse updateStock(Long itemId, UpdateStockRequest request);
    ReliefItemResponse getById(Long itemId);
}
