package com.floodrescue.module.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @NotNull
    private Integer quantity; // số lượng thay đổi (dương = nhập, âm = xuất)

    private String note;
}