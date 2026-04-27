package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a registered retail customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /** Unique customer identifier. */
    private String customerId;

    /** Customer's first name. */
    private String firstName;

    /** Customer's last name. */
    private String lastName;

    /** Customer's email address. */
    private String email;

    /** Tier / membership level (e.g. SILVER, GOLD, PLATINUM). */
    private String membershipTier;
}
