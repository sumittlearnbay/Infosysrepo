package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single customer purchase transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** Unique transaction identifier. */
    private String transactionId;

    /** ID of the customer who made the purchase. */
    private String customerId;

    /** Amount spent in this transaction (USD). */
    private BigDecimal amount;

    /** Date the transaction occurred. */
    private LocalDate transactionDate;

    /** Optional description / merchant name. */
    private String description;
}
