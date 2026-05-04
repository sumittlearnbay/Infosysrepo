package com.retailer.rewards.exception;

/**
 * Thrown when a date range is invalid.
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
