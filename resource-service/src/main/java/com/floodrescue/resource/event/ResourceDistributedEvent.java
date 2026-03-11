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
public class ResourceDistributedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;

    private Long distributionId;
    private Long requestId;
    private Long recipientId;
    private Long warehouseId;
    private Integer totalItems;
    private LocalDateTime distributedAt;
}
