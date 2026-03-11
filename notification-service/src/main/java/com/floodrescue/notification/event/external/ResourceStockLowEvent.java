package com.floodrescue.notification.event.external;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DUPLICATED CLASS - TECHNICAL DEBT
 * Source of Truth: resource-service
 * Purpose: Local DTO for AMQP message consumption.
 * Syncing: Must be updated manually when the source class changes.
 */
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
