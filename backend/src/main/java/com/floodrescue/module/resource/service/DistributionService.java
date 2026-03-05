package com.floodrescue.module.resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.module.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.module.resource.dto.response.DistributionResponse;

public interface DistributionService {

    /**
     * Tạo phiếu phân phối hàng cứu trợ
     * Trừ tồn kho từng item → kiểm tra threshold → publish event
     */
    DistributionResponse create(CreateDistributionRequest request, Long coordinatorId);

    Page<DistributionResponse> getAll(Pageable pageable);

    Page<DistributionResponse> getByRequestId(Long requestId, Pageable pageable);
}