package com.floodrescue.module.resource.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDistributionRequest {

    @NotNull
    private Long requestId;

    @NotNull
    private Long recipientId;

    private String note;

    @NotEmpty(message = "Phải có ít nhất 1 hàng hóa")
    private List<DistributionItemRequest> items;

    @Data
    public static class DistributionItemRequest {
        @NotNull
        private Long reliefItemId;

        @Min(1)
        private Integer quantity;
    }
}