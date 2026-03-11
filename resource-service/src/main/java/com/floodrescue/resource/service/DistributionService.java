package com.floodrescue.resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.floodrescue.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.resource.dto.response.DistributionResponse;

public interface DistributionService {
    DistributionResponse create(CreateDistributionRequest request, Long coordinatorId);
    Page<DistributionResponse> getAll(Pageable pageable);
    Page<DistributionResponse> getByRequestId(Long requestId, Pageable pageable);
}
