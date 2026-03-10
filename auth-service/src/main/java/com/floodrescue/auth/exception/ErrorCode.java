package com.floodrescue.auth.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // ===== CHUNG =====
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),

    // ===== AUTH =====
    DUPLICATE_PHONE("DUPLICATE_PHONE", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED),
    ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", HttpStatus.FORBIDDEN);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
