package com.floodrescue.auth.dtos.response;

import com.floodrescue.auth.enums.RoleType;
import com.floodrescue.auth.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private RoleType role;
    private UserStatus status;
    private BigDecimal lat;
    private BigDecimal lng;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
