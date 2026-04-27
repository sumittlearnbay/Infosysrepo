package com.retailer.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Retailer Rewards API.
 * <p>
 * Rewards Calculation Rules:
 * - 2 points for every dollar spent over $100 in a single transaction
 * - 1 point for every dollar spent between $50 and $100 in a single transaction
 * <p>
 * Example: A $120 purchase = (2 x $20) + (1 x $50) = 90 points
 */
@SpringBootApplication
public class RewardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RewardsApplication.class, args);
    }
}
