package com.floodrescue.module.rescue_request.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRescueRequestDto {

    @NotNull(message = "Vĩ độ không được để trống")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal lat;

    @NotNull(message = "Kinh độ không được để trống")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal lng;

    @Size(max = 500)
    private String addressText;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 2000)
    private String description;

    @Min(value = 1)
    @Max(value = 100)
    private Integer numPeople = 1;

    // urgencyLevel tự động phân loại qua UrgencyClassificationService
    // citizen không cần truyền lên — để null, service sẽ tính
}