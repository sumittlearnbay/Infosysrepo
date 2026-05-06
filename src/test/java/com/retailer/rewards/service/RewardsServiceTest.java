package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.retailer.rewards.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RewardsServiceTest {

    @Autowired
    private RewardsService service;

    @Autowired
    private TransactionRepository repository;

    @Test
    void testCalculateRewardsForLastNMonths_success() {
        RewardsResponse response = service.calculateRewardsForLastNMonths(CUSTOMER_ID, DEFAULT_MONTHS);

        assertNotNull(response);
        assertEquals(CUSTOMER_ID, response.getCustomerId());
        assertEquals(CUSTOMER_NAME, response.getCustomerName());
        assertEquals(SEEDED_CUSTOMER_COUNT, response.getTotalTransactions());
        assertEquals(0, EXPECTED_CUSTOMER_REWARD_POINTS.compareTo(response.getTotalRewardPoints()));
        assertFalse(response.getMonthlyBreakdown().isEmpty());
    }

    @Test
    void testCalculateRewardsByDateRange_success() {
        RewardsResponse response = service.calculateRewardsByDateRange(
                CUSTOMER_ID,
                LocalDate.now().minusMonths(DEFAULT_MONTHS),
                LocalDate.now());

        assertNotNull(response);
        assertEquals(CUSTOMER_ID, response.getCustomerId());
        assertEquals(SEEDED_CUSTOMER_COUNT, response.getTotalTransactions());
        assertEquals(0, EXPECTED_CUSTOMER_REWARD_POINTS.compareTo(response.getTotalRewardPoints()));
    }

    @Test
    void testCalculateRewardsForAllCustomers() {
        List<RewardsResponse> responses = service.calculateRewardsForAllCustomers(DEFAULT_MONTHS);

        assertEquals(SEEDED_CUSTOMER_COUNT, responses.size());
        assertTrue(responses.stream().anyMatch(response -> CUSTOMER_ID.equals(response.getCustomerId())));
    }

    @Test
    void testCustomerNotFound_lastNMonths() {
        assertThrows(CustomerNotFoundException.class,
                () -> service.calculateRewardsForLastNMonths(INVALID_ID, DEFAULT_MONTHS));
    }

    @Test
    void testCustomerNotFound_dateRange() {
        assertThrows(CustomerNotFoundException.class,
                () -> service.calculateRewardsByDateRange(
                        INVALID_ID,
                        LocalDate.now().minusMonths(DEFAULT_MONTHS),
                        LocalDate.now()));
    }

    @Test
    void testNoTransactionsForDateRange() {
        RewardsResponse response = service.calculateRewardsByDateRange(
                CUSTOMER_ID,
                LocalDate.now().minusYears(1),
                LocalDate.now().minusYears(1).plusDays(1));

        assertEquals(ZERO_COUNT, response.getTotalTransactions());
        assertEquals(ZERO_COUNT, response.getTotalRewardPoints().intValue());
        assertTrue(response.getMonthlyBreakdown().isEmpty());
    }

    @Test
    void testNegativeAmountThrowsException() {
        repository.addTransaction(Transaction.builder()
                .transactionId(NEW_TRANSACTION_ID)
                .customerId(CUSTOMER_ID)
                .transactionDate(LocalDate.now())
                .amount(NEGATIVE_AMOUNT)
                .description(MOCK_DESCRIPTION_1)
                .build());

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateRewardsForLastNMonths(CUSTOMER_ID, DEFAULT_MONTHS));
    }
}
