package com.retailer.rewards.exception;

/**
 * Thrown when a customer is not found.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}
