package com.floodrescue.resource.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
