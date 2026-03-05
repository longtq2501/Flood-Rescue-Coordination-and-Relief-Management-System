package com.floodrescue.module.resource.event;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceStockLowEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long itemId;
    private String itemName;
    private Long warehouseId;
    private String warehouseName;
    private Integer currentQuantity;
    private Integer threshold;
    private String unit;
}