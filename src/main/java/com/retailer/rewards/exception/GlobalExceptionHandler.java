package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST API.
 * Provides centralized and consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // 🔹 CUSTOMER NOT FOUND
    // =========================
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // =========================
    // 🔹 INVALID DATE RANGE
    // =========================
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // =========================
    // 🔹 ILLEGAL ARGUMENT
    // =========================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // =========================
    // 🔹 MISSING PARAMETER
    // =========================
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex) {

        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // =========================
    // 🔹 TYPE MISMATCH
    // =========================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String type = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "Invalid";

        String message = String.format(
                "Invalid %s format for parameter '%s'",
                type,
                ex.getName()
        );

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    // =========================
    // 🔹 GENERIC EXCEPTION
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    // =========================
    // 🔹 COMMON BUILDER METHOD
    // =========================
    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message) {

        ApiErrorResponse error = ApiErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}