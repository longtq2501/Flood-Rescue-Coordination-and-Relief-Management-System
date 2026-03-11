package com.floodrescue.resource.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.floodrescue.resource.domain.entity.Distribution;
import com.floodrescue.resource.domain.entity.DistributionItem;
import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.dto.request.CreateDistributionRequest;
import com.floodrescue.resource.dto.response.DistributionResponse;
import com.floodrescue.resource.event.ResourceDistributedEvent;
import com.floodrescue.resource.event.ResourceEventPublisher;
import com.floodrescue.resource.event.ResourceStockLowEvent;
import com.floodrescue.resource.repository.DistributionRepository;
import com.floodrescue.resource.repository.ReliefItemRepository;
import com.floodrescue.resource.service.DistributionService;
import com.floodrescue.resource.shared.exception.AppException;
import com.floodrescue.resource.shared.exception.ErrorCode;

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
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
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
            var warehouseIds = impactedItems.stream()
                    .map(item -> item.getWarehouse().getId())
                    .distinct()
                    .toList();

            eventPublisher.publishDistributed(ResourceDistributedEvent.builder()
                    .distributionId(savedDistribution.getId())
                    .requestId(savedDistribution.getRequestId())
                    .recipientId(savedDistribution.getRecipientId())
                    .warehouseId(warehouseIds.size() == 1 ? warehouseIds.get(0) : null)
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
    @Transactional(readOnly = true)
    public Page<DistributionResponse> getAll(Pageable pageable) {
        Page<Distribution> page = distributionRepository.findAll(pageable);
        List<Distribution> withItems = distributionRepository.fetchWithItems(page.getContent());

        // Map withItems vào page content
        Map<Long, Distribution> itemsMap = withItems.stream()
                .collect(Collectors.toMap(Distribution::getId, d -> d));

        return page.map(d -> toResponse(itemsMap.getOrDefault(d.getId(), d)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DistributionResponse> getByRequestId(Long requestId, Pageable pageable) {
        Page<Distribution> page = distributionRepository.findByRequestId(requestId, pageable);
        List<Distribution> withItems = distributionRepository.fetchWithItems(page.getContent());

        Map<Long, Distribution> itemsMap = withItems.stream()
                .collect(Collectors.toMap(Distribution::getId, d -> d));

        return page.map(d -> toResponse(itemsMap.getOrDefault(d.getId(), d)));
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
