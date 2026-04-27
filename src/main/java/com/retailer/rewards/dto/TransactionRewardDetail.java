package com.retailer.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary of rewards earned in a single transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRewardDetail {

    private String transactionId;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private long pointsEarned;
    private String description;
}
