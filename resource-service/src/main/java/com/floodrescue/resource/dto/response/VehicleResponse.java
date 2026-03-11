package com.floodrescue.resource.dto.response;

import java.math.BigDecimal;

import com.floodrescue.resource.domain.enums.VehicleStatus;
import com.floodrescue.resource.domain.enums.VehicleType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleResponse {
    private Long id;
    private String plateNumber;
    private VehicleType type;
    private Integer capacity;
    private VehicleStatus status;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private Long assignedTeamId;
}
