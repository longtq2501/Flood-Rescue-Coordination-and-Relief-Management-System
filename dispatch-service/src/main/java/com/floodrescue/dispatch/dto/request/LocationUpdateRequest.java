package com.floodrescue.dispatch.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateRequest {

    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;

    private BigDecimal speed;
    private BigDecimal heading;
}
