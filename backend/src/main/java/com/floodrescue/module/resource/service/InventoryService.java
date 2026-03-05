package com.floodrescue.module.resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.module.resource.dto.request.CreateReliefItemRequest;
import com.floodrescue.module.resource.dto.request.UpdateStockRequest;
import com.floodrescue.module.resource.dto.response.ReliefItemResponse;

public interface InventoryService {

    ReliefItemResponse addItem(CreateReliefItemRequest request);

    Page<ReliefItemResponse> getItemsByWarehouse(Long warehouseId, Pageable pageable);

    /**
     * Cập nhật số lượng tồn kho
     * quantity dương = nhập thêm, âm = xuất ra
     * Sau khi update → kiểm tra threshold → publish event nếu cần
     */
    ReliefItemResponse updateStock(Long itemId, UpdateStockRequest request);

    ReliefItemResponse getById(Long itemId);
}