package com.retailer.rewards.exception;

/**
 * Thrown when a requested customer ID does not exist in the data store.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
    }
}
