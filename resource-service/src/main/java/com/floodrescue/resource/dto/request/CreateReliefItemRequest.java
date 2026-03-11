package com.floodrescue.resource.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReliefItemRequest {

    @NotNull
    private Long warehouseId;

    @NotBlank(message = "Tên hàng hóa không được để trống")
    private String name;

    private String category;

    @NotBlank(message = "Đơn vị không được để trống")
    private String unit;

    @Min(0)
    private Integer quantity = 0;

    @Min(1)
    private Integer lowThreshold = 10;
}
