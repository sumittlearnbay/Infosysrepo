package com.retailer.rewards.service;

import com.retailer.rewards.dto.MonthlyRewardsSummary;
import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.dto.TransactionRewardDetail;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business logic for the Rewards Program.
 *
 * <h3>Calculation Rules</h3>
 * <ul>
 *   <li>2 points per dollar spent <strong>over $100</strong> in a single transaction</li>
 *   <li>1 point per dollar spent <strong>between $50 and $100</strong> (inclusive) in a single transaction</li>
 *   <li>No points for amounts at or below $50</li>
 * </ul>
 *
 * <h3>Example</h3>
 * $120 purchase → (2 × $20) + (1 × $50) = <strong>90 points</strong>
 */
@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);

    private static final BigDecimal TIER_ONE_THRESHOLD  = new BigDecimal("50");
    private static final BigDecimal TIER_TWO_THRESHOLD  = new BigDecimal("100");
    private static final int        MAX_MONTHS          = 36;   // sanity cap

    private final TransactionRepository transactionRepository;

    public RewardsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Calculates rewards for a customer over the last {@code months} calendar months
     * (ending today).
     *
     * @param customerId the customer to query
     * @param months     number of months to look back (1–36)
     * @return fully populated {@link RewardsResponse}
     * @throws CustomerNotFoundException  if no customer exists with the given ID
     * @throws InvalidDateRangeException  if {@code months} is outside [1, 36]
     */
    public RewardsResponse calculateRewardsForLastNMonths(String customerId, int months) {
        validateMonths(months);

        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months).plusDays(1);

        log.info("Calculating rewards for customer={} from {} to {} ({} months)",
                customerId, startDate, endDate, months);

        return calculateRewards(customerId, startDate, endDate);
    }

    /**
     * Calculates rewards for a customer within an explicit date range.
     *
     * @param customerId the customer to query
     * @param startDate  period start (inclusive)
     * @param endDate    period end   (inclusive)
     * @return fully populated {@link RewardsResponse}
     * @throws CustomerNotFoundException  if no customer exists with the given ID
     * @throws InvalidDateRangeException  if the range is logically invalid
     */
    public RewardsResponse calculateRewardsByDateRange(
            String customerId, LocalDate startDate, LocalDate endDate) {

        validateDateRange(startDate, endDate);

        log.info("Calculating rewards for customer={} from {} to {}",
                customerId, startDate, endDate);

        return calculateRewards(customerId, startDate, endDate);
    }

    /**
     * Calculates rewards for <em>all</em> customers over the last {@code months} months.
     *
     * @param months number of months to look back (1–36)
     * @return list of {@link RewardsResponse}, one per customer
     * @throws InvalidDateRangeException if {@code months} is outside [1, 36]
     */
    public List<RewardsResponse> calculateRewardsForAllCustomers(int months) {
        validateMonths(months);

        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months).plusDays(1);

        log.info("Calculating rewards for ALL customers from {} to {}", startDate, endDate);

        return transactionRepository.findAllCustomers()
                .stream()
                .map(customer -> calculateRewards(customer.getCustomerId(), startDate, endDate))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the reward points earned for a single transaction amount.
     * Exposed as a public utility so it can be tested independently.
     *
     * @param amount transaction amount (must be non-negative)
     * @return points earned
     */
    public long calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction amount must not be negative");
        }

        long points = 0L;

        if (amount.compareTo(TIER_TWO_THRESHOLD) > 0) {
            // Dollars above $100 earn 2 points each
            BigDecimal aboveHundred = amount.subtract(TIER_TWO_THRESHOLD);
            points += aboveHundred.longValue() * 2;

            // Dollars between $50 and $100 earn 1 point each
            points += TIER_TWO_THRESHOLD.subtract(TIER_ONE_THRESHOLD).longValue();

        } else if (amount.compareTo(TIER_ONE_THRESHOLD) > 0) {
            // Dollars between $50 and $100 earn 1 point each
            BigDecimal betweenFiftyAndHundred = amount.subtract(TIER_ONE_THRESHOLD);
            points += betweenFiftyAndHundred.longValue();
        }

        return points;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private RewardsResponse calculateRewards(
            String customerId, LocalDate startDate, LocalDate endDate) {

        Customer customer = transactionRepository.findCustomerById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        List<Transaction> transactions =
                transactionRepository.findTransactionsByCustomerIdAndDateRange(
                        customerId, startDate, endDate);

        log.debug("Found {} transactions for customer={}", transactions.size(), customerId);

        // Group transactions by YearMonth
        Map<YearMonth, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getTransactionDate()),
                        TreeMap::new,
                        Collectors.toList()));

        List<MonthlyRewardsSummary> monthlyBreakdown = new ArrayList<>();
        long totalPoints = 0L;

        for (Map.Entry<YearMonth, List<Transaction>> entry : byMonth.entrySet()) {
            YearMonth ym = entry.getKey();
            List<Transaction> monthTxns = entry.getValue();

            List<TransactionRewardDetail> details = new ArrayList<>();
            long monthlyPoints = 0L;

            for (Transaction t : monthTxns) {
                long pts = calculatePoints(t.getAmount());
                monthlyPoints += pts;
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

            totalPoints += monthlyPoints;
        }

        int monthsCovered = (int) YearMonth.from(startDate)
                .until(YearMonth.from(endDate).plusMonths(1),
                        java.time.temporal.ChronoUnit.MONTHS);

        return RewardsResponse.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .membershipTier(customer.getMembershipTier())
                .periodStart(startDate)
                .periodEnd(endDate)
                .monthsCovered(monthsCovered)
                .monthlyBreakdown(monthlyBreakdown)
                .totalTransactions(transactions.size())
                .totalRewardPoints(totalPoints)
                .build();
    }

    private void validateMonths(int months) {
        if (months < 1 || months > MAX_MONTHS) {
            throw new InvalidDateRangeException(
                    "months must be between 1 and " + MAX_MONTHS + ", but was: " + months);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("startDate and endDate must not be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException(
                    "endDate (" + endDate + ") must not be before startDate (" + startDate + ")");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new InvalidDateRangeException(
                    "startDate (" + startDate + ") must not be in the future");
        }
    }
}
