package com.floodrescue.resource.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReliefItemResponse {
    private Long id;
    private Long warehouseId;
    private String name;
    private String category;
    private String unit;
    private Integer quantity;
    private Integer lowThreshold;
    private Boolean belowThreshold;
    private LocalDateTime updatedAt;
}
