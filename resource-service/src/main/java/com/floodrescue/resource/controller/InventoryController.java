package com.floodrescue.resource.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.floodrescue.resource.dto.request.CreateReliefItemRequest;
import com.floodrescue.resource.dto.request.UpdateStockRequest;
import com.floodrescue.resource.dto.response.ReliefItemResponse;
import com.floodrescue.resource.service.InventoryService;
import com.floodrescue.resource.shared.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources/items")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReliefItemResponse>> addItem(
            @Valid @RequestBody CreateReliefItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryService.addItem(request), "Thêm hàng hóa thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReliefItemResponse>>> getByWarehouse(
            @RequestParam Long warehouseId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getItemsByWarehouse(warehouseId, pageable)));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ReliefItemResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.updateStock(id, request), "Cập nhật tồn kho thành công"));
    }
}
