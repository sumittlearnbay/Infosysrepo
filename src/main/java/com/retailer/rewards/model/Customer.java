package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a customer in the rewards program.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private String customerId;
    private String name;
    private String email;
    private String membershipTier;
}
