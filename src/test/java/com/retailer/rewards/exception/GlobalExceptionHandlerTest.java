package com.retailer.rewards.exception;

import com.retailer.rewards.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.retailer.rewards.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

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
        CustomerNotFoundException ex = new CustomerNotFoundException(CUSTOMER_ID);

        ResponseEntity<ApiErrorResponse> response = handler.handleCustomerNotFound(ex);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains(CUSTOMER_ID));
        assertNotNull(response.getBody().getTimestamp());
    }
    // =========================
    // ✅ INVALID DATE RANGE
    // =========================

    @Test
    void testHandleInvalidDateRange() {
        InvalidDateRangeException ex = new InvalidDateRangeException(INVALID_DATE_RANGE_MESSAGE);

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidDateRange(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(INVALID_DATE_RANGE_MESSAGE, response.getBody().getMessage());
    }

    // =========================
    // ✅ ILLEGAL ARGUMENT
    // =========================

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException(BAD_INPUT_MESSAGE);

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(BAD_INPUT_MESSAGE, response.getBody().getMessage());
    }

    // =========================
    // ✅ MISSING PARAMETER
    // =========================

    @Test
    void testHandleMissingParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException(CUSTOMER_ID_PARAM, STRING_TYPE);

        ResponseEntity<ApiErrorResponse> response = handler.handleMissingParameter(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains(CUSTOMER_ID_PARAM));
    }

    // =========================
    // ✅ TYPE MISMATCH
    // =========================

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        NON_NUMERIC_VALUE, Integer.class, MONTHS_PARAM, null, new Throwable()
                );

        ResponseEntity<ApiErrorResponse> response = handler.handleTypeMismatch(ex);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().getMessage().contains(MONTHS_PARAM));
    }

    // =========================
    // ✅ GENERIC EXCEPTION
    // =========================

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception(GENERIC_EXCEPTION_MESSAGE);

        ResponseEntity<ApiErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals(GENERIC_ERROR_RESPONSE, response.getBody().getMessage());
    }
}
