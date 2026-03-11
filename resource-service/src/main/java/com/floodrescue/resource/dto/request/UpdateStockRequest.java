package com.floodrescue.resource.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @NotNull
    private Integer quantity;

    private String note;
}
