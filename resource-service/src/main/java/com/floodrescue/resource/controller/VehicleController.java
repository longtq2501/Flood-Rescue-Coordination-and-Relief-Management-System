package com.floodrescue.resource.controller;

import java.util.List;

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

import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;
import com.floodrescue.resource.dto.request.CreateVehicleRequest;
import com.floodrescue.resource.dto.response.VehicleResponse;
import com.floodrescue.resource.service.VehicleService;
import com.floodrescue.resource.shared.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(vehicleService.create(request), "Thêm xe thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getAll(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) VehicleType type) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getAll(status, type)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam VehicleStatus status,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.updateStatus(id, status, note), "Cập nhật trạng thái thành công"));
    }
}
