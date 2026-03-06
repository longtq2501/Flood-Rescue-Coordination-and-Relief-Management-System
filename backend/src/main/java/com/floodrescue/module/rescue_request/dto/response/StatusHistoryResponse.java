package com.floodrescue.module.rescue_request.dto.response;

import java.time.LocalDateTime;

import com.floodrescue.module.rescue_request.domain.enums.RequestStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusHistoryResponse {
    private RequestStatus fromStatus;
    private RequestStatus toStatus;
    private Long changedBy;
    private String note;
    private LocalDateTime changedAt;
}