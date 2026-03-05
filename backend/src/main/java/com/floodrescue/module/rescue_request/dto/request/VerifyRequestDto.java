package com.floodrescue.module.rescue_request.dto.request;

import com.floodrescue.module.rescue_request.domain.enums.UrgencyLevel;

import lombok.Data;

@Data
public class VerifyRequestDto {
    private UrgencyLevel urgencyLevel; // coordinator có thể override urgency
    private String note;
}