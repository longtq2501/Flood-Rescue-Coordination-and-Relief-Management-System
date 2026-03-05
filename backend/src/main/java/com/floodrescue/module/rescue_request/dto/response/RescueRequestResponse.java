package com.floodrescue.module.rescue_request.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.floodrescue.module.rescue_request.domain.enums.RequestStatus;
import com.floodrescue.module.rescue_request.domain.enums.UrgencyLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueRequestResponse {
    private Long id;
    private Long citizenId;
    private BigDecimal lat;
    private BigDecimal lng;
    private String addressText;
    private String description;
    private Integer numPeople;
    private UrgencyLevel urgencyLevel;
    private RequestStatus status;
    private Long coordinatorId;
    private List<String> imageUrls; // presigned URLs từ MinIO
    private List<StatusHistoryResponse> statusHistories;
    private LocalDateTime verifiedAt;
    private LocalDateTime completedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
}