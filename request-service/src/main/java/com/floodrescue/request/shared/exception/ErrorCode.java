package com.floodrescue.request.shared.exception;

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

    // ===== RESCUE REQUEST =====
    REQUEST_NOT_FOUND("REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND),
    REQUEST_ALREADY_ACTIVE("REQUEST_ALREADY_ACTIVE", HttpStatus.BAD_REQUEST),
    REQUEST_INVALID_STATUS("REQUEST_INVALID_STATUS", HttpStatus.BAD_REQUEST),
    REQUEST_FORBIDDEN("REQUEST_FORBIDDEN", HttpStatus.FORBIDDEN);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
