package com.retailer.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Top-level API response for the rewards query endpoint.
 * Contains customer info, monthly breakdowns, and the grand total.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsResponse {

    // ── Customer Information ─────────────────────────────────────────────────
    private String customerId;
    private String customerName;
    private String email;
    private String membershipTier;

    // ── Query Window ─────────────────────────────────────────────────────────
    /** Start of the period being evaluated (inclusive). */
    private LocalDate periodStart;

    /** End of the period being evaluated (inclusive). */
    private LocalDate periodEnd;

    /** Number of months covered. */
    private int monthsCovered;

    // ── Rewards Breakdown ────────────────────────────────────────────────────
    /** Reward points grouped by calendar month. */
    private List<MonthlyRewardsSummary> monthlyBreakdown;

    /** Total transactions in the period. */
    private int totalTransactions;

    /** Grand total of reward points across all months. */
    private BigDecimal totalRewardPoints;
}
