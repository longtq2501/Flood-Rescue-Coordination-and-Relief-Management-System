package com.floodrescue.resource.shared.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.floodrescue.resource.shared.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Void>> handlingRuntimeException(RuntimeException exception) {
        log.error("Exception: ", exception);
        return ResponseEntity.badRequest().body(
                ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage(), 
                                  String.valueOf(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())));
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.error(errorCode.getMessage(), String.valueOf(errorCode.getCode())));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Log if needed
        }
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.error(errorCode.getMessage(), String.valueOf(errorCode.getCode())));
    }
}
