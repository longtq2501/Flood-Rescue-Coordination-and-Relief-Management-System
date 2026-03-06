package com.floodrescue.module.resource.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private Long managerId;
    private LocalDateTime createdAt;
    private List<ReliefItemResponse> items;
}