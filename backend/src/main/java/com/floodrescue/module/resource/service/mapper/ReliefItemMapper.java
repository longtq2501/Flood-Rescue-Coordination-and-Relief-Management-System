package com.floodrescue.module.resource.service.mapper;

import com.floodrescue.module.resource.domain.entity.ReliefItem;
import com.floodrescue.module.resource.dto.response.ReliefItemResponse;

public final class ReliefItemMapper {

    private ReliefItemMapper() {
    }

    public static ReliefItemResponse toResponse(ReliefItem item) {
        return ReliefItemResponse.builder()
                .id(item.getId())
                .warehouseId(item.getWarehouse().getId())
                .name(item.getName())
                .category(item.getCategory())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .lowThreshold(item.getLowThreshold())
                .belowThreshold(item.isBelowThreshold())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
