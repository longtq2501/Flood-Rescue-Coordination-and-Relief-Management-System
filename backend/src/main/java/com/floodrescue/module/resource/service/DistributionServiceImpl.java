package com.floodrescue.module.resource.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.resource.domain.entity.Distribution;
import com.floodrescue.module.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.module.resource.dto.response.DistributionResponse;
import com.floodrescue.module.resource.event.ResourceEventPublisher;
import com.floodrescue.module.resource.repository.DistributionRepository;
import com.floodrescue.module.resource.repository.ReliefItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {

    private final DistributionRepository distributionRepository;
    private final ReliefItemRepository reliefItemRepository;
    private final ResourceEventPublisher eventPublisher;

    @Override
    @Transactional
    public DistributionResponse create(CreateDistributionRequest request, Long coordinatorId) {
        // TODO Tiến: implement
        //
        // Step 1: Validate từng item có đủ tồn kho không
        // for each item in request.getItems():
        // ReliefItem reliefItem = reliefItemRepository.findById(item.getReliefItemId())
        // .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        // if (reliefItem.getQuantity() < item.getQuantity())
        // throw new AppException(ErrorCode.VALIDATION_ERROR,
        // "Không đủ tồn kho: " + reliefItem.getName());
        //
        // Step 2: Tạo Distribution entity
        //
        // Step 3: Tạo từng DistributionItem + trừ tồn kho
        // reliefItem.setQuantity(reliefItem.getQuantity() - item.getQuantity());
        // reliefItemRepository.save(reliefItem);
        //
        // Step 4: Lưu Distribution vào DB
        //
        // Step 5: Publish distributed event
        // Lấy warehouseId từ item đầu tiên trong distribution:
        // Long firstItemId = request.getItems().get(0).getReliefItemId();
        // ReliefItem firstItem =
        // reliefItemRepository.findById(firstItemId).orElseThrow();
        // Long warehouseId = firstItem.getWarehouse().getId();
        //
        // eventPublisher.publishDistributed(ResourceDistributedEvent.builder()
        // .distributionId(savedDistribution.getId())
        // .requestId(request.getRequestId())
        // .recipientId(request.getRecipientId())
        // .warehouseId(warehouseId) // ← thêm vào đây
        // .totalItems(request.getItems().size())
        // .distributedAt(savedDistribution.getDistributedAt())
        // .build());
        //
        // Step 6: Kiểm tra từng item có xuống dưới threshold không
        // if (reliefItem.isBelowThreshold())
        // eventPublisher.publishStockLow(ResourceStockLowEvent.builder()
        // .itemId(reliefItem.getId())
        // .itemName(reliefItem.getName())
        // .warehouseId(reliefItem.getWarehouse().getId())
        // .warehouseName(reliefItem.getWarehouse().getName())
        // .currentQuantity(reliefItem.getQuantity())
        // .threshold(reliefItem.getLowThreshold())
        // .unit(reliefItem.getUnit())
        // .build());
        //
        // Step 7: Trả về DistributionResponse
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    @Override
    public Page<DistributionResponse> getAll(Pageable pageable) {
        // TODO Tiến: implement
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    @Override
    public Page<DistributionResponse> getByRequestId(Long requestId, Pageable pageable) {
        // TODO Tiến: implement
        throw new UnsupportedOperationException("TODO: Tiến implement");
    }

    // ==================== PRIVATE HELPERS ====================

    private DistributionResponse toResponse(Distribution d) {
        // TODO Tiến: implement mapping
        throw new UnsupportedOperationException("TODO: Tiến implement toResponse()");
    }
}