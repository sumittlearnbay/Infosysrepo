package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // =========================
    // ✅ CUSTOMER NOT FOUND
    // =========================

    @Test
    void testHandleCustomerNotFound() {
        CustomerNotFoundException ex = new CustomerNotFoundException("C001");

        ResponseEntity<ApiErrorResponse> response = handler.handleCustomerNotFound(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains("C001"));
        assertNotNull(response.getBody().getTimestamp());
    }
    // =========================
    // ✅ INVALID DATE RANGE
    // =========================

    @Test
    void testHandleInvalidDateRange() {
        InvalidDateRangeException ex = new InvalidDateRangeException("Invalid date range");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidDateRange(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid date range", response.getBody().getMessage());
    }

    // =========================
    // ✅ ILLEGAL ARGUMENT
    // =========================

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Bad input", response.getBody().getMessage());
    }

    // =========================
    // ✅ MISSING PARAMETER
    // =========================

    @Test
    void testHandleMissingParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("customerId", "String");

        ResponseEntity<ApiErrorResponse> response = handler.handleMissingParameter(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains("customerId"));
    }

    // =========================
    // ✅ TYPE MISMATCH
    // =========================

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        "abc", Integer.class, "months", null, new Throwable()
                );

        ResponseEntity<ApiErrorResponse> response = handler.handleTypeMismatch(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains("months"));
    }

    // =========================
    // ✅ GENERIC EXCEPTION
    // =========================

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected");

        ResponseEntity<ApiErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}