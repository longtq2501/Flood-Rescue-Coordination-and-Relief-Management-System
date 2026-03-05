package com.floodrescue.module.resource.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.floodrescue.module.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.module.resource.dto.response.DistributionResponse;
import com.floodrescue.module.resource.service.DistributionService;
import com.floodrescue.shared.response.ApiResponse;
import com.floodrescue.shared.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources/distributions")
@RequiredArgsConstructor
public class DistributionController {

    private final DistributionService distributionService;

    @PostMapping
    public ResponseEntity<ApiResponse<DistributionResponse>> create(
            @Valid @RequestBody CreateDistributionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Phân phối thành công",
                        distributionService.create(request, principal.getId())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DistributionResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                distributionService.getAll(pageable)));
    }

    @GetMapping("/by-request/{requestId}")
    public ResponseEntity<ApiResponse<Page<DistributionResponse>>> getByRequestId(
            @PathVariable Long requestId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("OK",
                distributionService.getByRequestId(requestId, pageable)));
    }
}