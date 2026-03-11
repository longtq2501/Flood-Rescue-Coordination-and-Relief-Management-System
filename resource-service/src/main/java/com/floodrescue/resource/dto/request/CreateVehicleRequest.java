package com.floodrescue.resource.dto.request;

import com.floodrescue.resource.domain.enums.VehicleType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVehicleRequest {

    @NotBlank(message = "Biển số không được để trống")
    private String plateNumber;

    private VehicleType type;

    private Integer capacity;
}
