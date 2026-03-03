package com.floodrescue.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private BigDecimal lat;
    private BigDecimal lng;
}