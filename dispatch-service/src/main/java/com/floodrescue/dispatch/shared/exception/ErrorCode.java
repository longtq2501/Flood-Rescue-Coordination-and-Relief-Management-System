package com.floodrescue.dispatch.shared.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // General
    INTERNAL_SERVER_ERROR("ERR_INTERNAL_500", "Lỗi server nội bộ"),
    UNAUTHORIZED("ERR_AUTH_401", "Chưa xác thực"),
    FORBIDDEN("ERR_AUTH_403", "Không có quyền truy cập"),
    VALIDATION_ERROR("ERR_VAL_400", "Lỗi dữ liệu đầu vào"),
    ENTITY_NOT_FOUND("ERR_NOT_FOUND", "Không tìm thấy dữ liệu"),

    // Dispatch specific
    TEAM_NOT_FOUND("ERR_TEAM_404", "Không tìm thấy đội cứu hộ"),
    TEAM_MEMBER_NOT_FOUND("ERR_TEAM_MEMBER_404", "Không tìm thấy thành viên đội cứu hộ"),
    TEAM_NOT_AVAILABLE("ERR_TEAM_400", "Đội cứu hộ đang bận hoặc không hoạt động"),
    ASSIGNMENT_NOT_FOUND("ERR_ASSIGNMENT_404", "Không tìm thấy thông tin phân công"),
    ASSIGNMENT_INVALID_STATUS("ERR_ASSIGNMENT_STATUS_400", "Trạng thái phân công không hợp lệ cho thao tác này"),
    PERMISSION_DENIED("ERR_PERMISSION_403", "Bạn không có quyền thực hiện thao tác này");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
