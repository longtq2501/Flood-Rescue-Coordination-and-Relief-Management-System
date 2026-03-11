package com.floodrescue.report.external.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDistributedEvent {
    private Long distributionId;
    private Long warehouseId;
}
