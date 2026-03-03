package com.floodrescue.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHORIZED        ("UNAUTHORIZED",         HttpStatus.UNAUTHORIZED),
    FORBIDDEN           ("FORBIDDEN",            HttpStatus.FORBIDDEN),
    NOT_FOUND           ("NOT_FOUND",            HttpStatus.NOT_FOUND),
    VALIDATION_ERROR    ("VALIDATION_ERROR",      HttpStatus.BAD_REQUEST),
    DUPLICATE_PHONE     ("DUPLICATE_PHONE",       HttpStatus.CONFLICT),
    DUPLICATE_EMAIL     ("DUPLICATE_EMAIL",       HttpStatus.CONFLICT),
    TEAM_UNAVAILABLE    ("TEAM_UNAVAILABLE",      HttpStatus.CONFLICT),
    VEHICLE_UNAVAILABLE ("VEHICLE_UNAVAILABLE",   HttpStatus.CONFLICT),
    INSUFFICIENT_STOCK  ("INSUFFICIENT_STOCK",    HttpStatus.CONFLICT),
    INTERNAL_ERROR      ("INTERNAL_ERROR",        HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String code, HttpStatus httpStatus) {
        this.code       = code;
        this.httpStatus = httpStatus;
    }
}