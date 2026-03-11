package com.floodrescue.resource.mapper;

import com.floodrescue.resource.domain.entity.ReliefItem;
import com.floodrescue.resource.dto.response.ReliefItemResponse;

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
