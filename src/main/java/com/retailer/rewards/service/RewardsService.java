package com.retailer.rewards.service;

import com.retailer.rewards.dto.MonthlyRewardsSummary;
import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.dto.TransactionRewardDetail;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating customer rewards.
 */
@Service
public class RewardsService {

    private static final BigDecimal TIER_ONE_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal TIER_TWO_THRESHOLD = new BigDecimal("100");

    private final TransactionRepository transactionRepository;

    public RewardsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates reward points for a single transaction amount.
     * Uses FLOOR rounding to ignore cents — only whole dollars count.
     *
     * @param amount transaction amount (must be non-negative)
     * @return points earned with proper decimal precision
     */
    private BigDecimal calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction amount must not be negative");
        }

        // Floor to ignore cents — only count whole dollars per requirement
        amount = amount.setScale(0, RoundingMode.FLOOR);

        BigDecimal points = BigDecimal.ZERO;

        if (amount.compareTo(TIER_TWO_THRESHOLD) > 0) {
            // Dollars above $100 earn 2 points each
            BigDecimal aboveHundred = amount.subtract(TIER_TWO_THRESHOLD);
            points = points.add(aboveHundred.multiply(new BigDecimal("2")));

            // Dollars between $50 and $100 earn 1 point each
            points = points.add(TIER_TWO_THRESHOLD.subtract(TIER_ONE_THRESHOLD));

        } else if (amount.compareTo(TIER_ONE_THRESHOLD) > 0) {
            // Dollars between $50 and $100 earn 1 point each
            BigDecimal betweenFiftyAndHundred = amount.subtract(TIER_ONE_THRESHOLD);
            points = points.add(betweenFiftyAndHundred);
        }

        return points;
    }

    /**
     * Calculates rewards for a single customer over the last N months.
     */
    public RewardsResponse calculateRewardsForLastNMonths(String customerId, int months) {
        Customer customer = transactionRepository.findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months).withDayOfMonth(1);

        return buildRewardsResponse(customer, startDate, endDate);
    }

    /**
     * Calculates rewards for a single customer within a date range.
     */
    public RewardsResponse calculateRewardsByDateRange(String customerId, LocalDate startDate, LocalDate endDate) {
        Customer customer = transactionRepository.findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return buildRewardsResponse(customer, startDate, endDate);
    }

    /**
     * Calculates rewards for all customers over the last N months.
     */
    public List<RewardsResponse> calculateRewardsForAllCustomers(int months) {
        return transactionRepository.findAllCustomers().stream()
                .map(customer -> calculateRewardsForLastNMonths(customer.getCustomerId(), months))
                .collect(Collectors.toList());
    }

    /**
     * Builds a complete rewards response for a customer and date range.
     */
    private RewardsResponse buildRewardsResponse(Customer customer, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository
                .findTransactionsByCustomerIdAndDateRange(customer.getCustomerId(), startDate, endDate);

        // Group transactions by YearMonth
        Map<YearMonth, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())));

        List<MonthlyRewardsSummary> monthlyBreakdown = new ArrayList<>();
        BigDecimal totalPoints = BigDecimal.ZERO;

        for (Map.Entry<YearMonth, List<Transaction>> entry : byMonth.entrySet()) {
            YearMonth ym = entry.getKey();
            List<Transaction> monthTxns = entry.getValue();

            List<TransactionRewardDetail> details = new ArrayList<>();
            BigDecimal monthlyPoints = BigDecimal.ZERO;

            for (Transaction t : monthTxns) {
                BigDecimal pts = calculatePoints(t.getAmount());
                monthlyPoints = monthlyPoints.add(pts);
                details.add(TransactionRewardDetail.builder()
                        .transactionId(t.getTransactionId())
                        .transactionDate(t.getTransactionDate())
                        .amount(t.getAmount())
                        .pointsEarned(pts)
                        .description(t.getDescription())
                        .build());
            }

            // Sort details by date ascending
            details.sort(Comparator.comparing(TransactionRewardDetail::getTransactionDate));

            monthlyBreakdown.add(MonthlyRewardsSummary.builder()
                    .year(ym.getYear())
                    .month(ym.getMonthValue())
                    .monthName(ym.getMonth().name())
                    .monthlyPoints(monthlyPoints)
                    .transactions(details)
                    .build());

            totalPoints = totalPoints.add(monthlyPoints);
        }

        // Sort monthly breakdown by year and month descending (most recent first)
        monthlyBreakdown.sort((a, b) -> {
            int yearCmp = Integer.compare(b.getYear(), a.getYear());
            return yearCmp != 0 ? yearCmp : Integer.compare(b.getMonth(), a.getMonth());
        });

        return RewardsResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getName())
                .email(customer.getEmail())
                .membershipTier(customer.getMembershipTier())
                .periodStart(startDate)
                .periodEnd(endDate)
                .monthsCovered(calculateMonthsCovered(startDate, endDate))
                .monthlyBreakdown(monthlyBreakdown)
                .totalTransactions(transactions.size())
                .totalRewardPoints(totalPoints)
                .build();
    }

    /**
     * Calculates the number of months between two dates (inclusive).
     */
    private int calculateMonthsCovered(LocalDate startDate, LocalDate endDate) {
        return (int) (YearMonth.from(endDate).compareTo(YearMonth.from(startDate)) + 1);
    }
}
