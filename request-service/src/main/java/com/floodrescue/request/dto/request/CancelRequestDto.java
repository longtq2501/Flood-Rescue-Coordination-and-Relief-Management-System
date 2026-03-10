package com.floodrescue.request.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelRequestDto {

    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;
}
