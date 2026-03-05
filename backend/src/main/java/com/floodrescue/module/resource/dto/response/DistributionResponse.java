package com.floodrescue.module.resource.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistributionResponse {
    private Long id;
    private Long requestId;
    private Long recipientId;
    private Long coordinatorId;
    private String note;
    private LocalDateTime distributedAt;
    private List<DistributionItemResponse> items;

    @Data
    @Builder
    public static class DistributionItemResponse {
        private Long reliefItemId;
        private String itemName;
        private String unit;
        private Integer quantity;
    }
}