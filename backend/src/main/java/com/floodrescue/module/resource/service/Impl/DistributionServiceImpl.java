package com.floodrescue.module.resource.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.module.resource.domain.entity.Distribution;
import com.floodrescue.module.resource.domain.entity.DistributionItem;
import com.floodrescue.module.resource.domain.entity.ReliefItem;
import com.floodrescue.module.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.module.resource.dto.response.DistributionResponse;
import com.floodrescue.module.resource.event.ResourceDistributedEvent;
import com.floodrescue.module.resource.event.ResourceEventPublisher;
import com.floodrescue.module.resource.event.ResourceStockLowEvent;
import com.floodrescue.module.resource.repository.DistributionRepository;
import com.floodrescue.module.resource.repository.ReliefItemRepository;
import com.floodrescue.module.resource.service.DistributionService;
import com.floodrescue.shared.exception.AppException;
import com.floodrescue.shared.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {

    private final DistributionRepository distributionRepository;
    private final ReliefItemRepository reliefItemRepository;
    private final ResourceEventPublisher eventPublisher;

    @Override
    @Transactional
    public DistributionResponse create(CreateDistributionRequest request, Long coordinatorId) {
        Distribution distribution = Distribution.builder()
                .requestId(request.getRequestId())
                .recipientId(request.getRecipientId())
                .coordinatorId(coordinatorId)
                .note(request.getNote())
                .build();

        List<ReliefItem> impactedItems = new ArrayList<>();
        ReliefItem firstItem = null;

        for (CreateDistributionRequest.DistributionItemRequest itemRequest : request.getItems()) {
            ReliefItem reliefItem = reliefItemRepository.findById(itemRequest.getReliefItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            if (reliefItem.getQuantity() < itemRequest.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK,
                        "Không đủ tồn kho: " + reliefItem.getName());
            }

            if (firstItem == null) {
                firstItem = reliefItem;
            }

            DistributionItem distributionItem = DistributionItem.builder()
                    .distribution(distribution)
                    .reliefItem(reliefItem)
                    .quantity(itemRequest.getQuantity())
                    .build();
            distribution.getItems().add(distributionItem);

            reliefItem.setQuantity(reliefItem.getQuantity() - itemRequest.getQuantity());
            impactedItems.add(reliefItem);
        }

        Distribution savedDistribution = distributionRepository.save(distribution);
        reliefItemRepository.saveAll(impactedItems);

        if (firstItem != null) {
            eventPublisher.publishDistributed(ResourceDistributedEvent.builder()
                    .distributionId(savedDistribution.getId())
                    .requestId(savedDistribution.getRequestId())
                    .recipientId(savedDistribution.getRecipientId())
                    .warehouseId(firstItem.getWarehouse().getId())
                    .totalItems(request.getItems().size())
                    .distributedAt(savedDistribution.getDistributedAt())
                    .build());
        }

        impactedItems.stream()
                .filter(ReliefItem::isBelowThreshold)
                .forEach(reliefItem -> eventPublisher.publishStockLow(ResourceStockLowEvent.builder()
                        .itemId(reliefItem.getId())
                        .itemName(reliefItem.getName())
                        .warehouseId(reliefItem.getWarehouse().getId())
                        .warehouseName(reliefItem.getWarehouse().getName())
                        .currentQuantity(reliefItem.getQuantity())
                        .threshold(reliefItem.getLowThreshold())
                        .unit(reliefItem.getUnit())
                        .build()));

        return toResponse(savedDistribution);
    }

    @Override
    public Page<DistributionResponse> getAll(Pageable pageable) {
        return distributionRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<DistributionResponse> getByRequestId(Long requestId, Pageable pageable) {
        return distributionRepository.findByRequestId(requestId, pageable).map(this::toResponse);
    }

    private DistributionResponse toResponse(Distribution distribution) {
        return DistributionResponse.builder()
                .id(distribution.getId())
                .requestId(distribution.getRequestId())
                .recipientId(distribution.getRecipientId())
                .coordinatorId(distribution.getCoordinatorId())
                .note(distribution.getNote())
                .distributedAt(distribution.getDistributedAt())
                .items(distribution.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .build();
    }

    private DistributionResponse.DistributionItemResponse toItemResponse(DistributionItem item) {
        return DistributionResponse.DistributionItemResponse.builder()
                .reliefItemId(item.getReliefItem().getId())
                .itemName(item.getReliefItem().getName())
                .unit(item.getReliefItem().getUnit())
                .quantity(item.getQuantity())
                .build();
    }
}
