package com.retailer.rewards.exception;

/**
 * Thrown when the supplied date range is logically invalid
 * (e.g. end date is before start date, or months value is out of range).
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
