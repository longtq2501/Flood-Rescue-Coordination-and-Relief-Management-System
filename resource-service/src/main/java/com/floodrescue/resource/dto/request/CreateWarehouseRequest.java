package com.floodrescue.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWarehouseRequest {

    @NotBlank(message = "Tên kho không được để trống")
    private String name;

    private String address;
}
