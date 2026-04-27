package com.retailer.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Rewards earned by a customer in a specific calendar month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRewardsSummary {

    /** Calendar year (e.g. 2024). */
    private int year;

    /** Calendar month (1 = January … 12 = December). */
    private int month;

    /** Human-readable month name (e.g. "JANUARY"). */
    private String monthName;

    /** Total reward points earned this month. */
    private long monthlyPoints;

    /** Breakdown per transaction. */
    private List<TransactionRewardDetail> transactions;
}
