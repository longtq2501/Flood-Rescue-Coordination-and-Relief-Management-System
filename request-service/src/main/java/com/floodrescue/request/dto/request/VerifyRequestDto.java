package com.floodrescue.request.dto.request;

import com.floodrescue.request.domain.enums.UrgencyLevel;

import lombok.Data;

@Data
public class VerifyRequestDto {
    private UrgencyLevel urgencyLevel;
    private String note;
}
