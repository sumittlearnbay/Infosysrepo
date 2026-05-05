package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private RewardsService service;

    private Customer customer;

    @BeforeEach
    void setup() {
        customer = Customer.builder()
                .customerId("C001")
                .name("Test User")
                .email("test@mail.com")
                .membershipTier("GOLD")
                .build();
    }

    // =========================
    // ✅ SUCCESS CASES
    // =========================

    @Test
    void testCalculateRewardsForLastNMonths_success() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.of(customer));

        when(repository.findTransactionsByCustomerIdAndDateRange(any(), any(), any()))
                .thenReturn(mockTransactions());

        RewardsResponse response =
                service.calculateRewardsForLastNMonths("C001", 3);

        assertNotNull(response);
        assertEquals("C001", response.getCustomerId());
        assertTrue(response.getTotalRewardPoints().compareTo(BigDecimal.ZERO) > 0);
        assertFalse(response.getMonthlyBreakdown().isEmpty());
    }

    @Test
    void testCalculateRewardsByDateRange_success() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.of(customer));

        when(repository.findTransactionsByCustomerIdAndDateRange(any(), any(), any()))
                .thenReturn(mockTransactions());

        RewardsResponse response =
                service.calculateRewardsByDateRange(
                        "C001",
                        LocalDate.now().minusMonths(1),
                        LocalDate.now()
                );

        assertNotNull(response);
        assertEquals("C001", response.getCustomerId());
    }

    @Test
    void testCalculateRewardsForAllCustomers() {
        when(repository.findAllCustomers())
                .thenReturn(Collections.singletonList(customer));

        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.of(customer));

        when(repository.findTransactionsByCustomerIdAndDateRange(any(), any(), any()))
                .thenReturn(mockTransactions());

        assertEquals(1,
                service.calculateRewardsForAllCustomers(3).size());
    }

    // =========================
    // ❌ EXCEPTION CASES
    // =========================

    @Test
    void testCustomerNotFound_lastNMonths() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> service.calculateRewardsForLastNMonths("C001", 3));
    }

    @Test
    void testCustomerNotFound_dateRange() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> service.calculateRewardsByDateRange(
                        "C001",
                        LocalDate.now(),
                        LocalDate.now()
                ));
    }

    // =========================
    // ⚡ EDGE CASES
    // =========================

    @Test
    void testNoTransactions() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.of(customer));

        when(repository.findTransactionsByCustomerIdAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        RewardsResponse response =
                service.calculateRewardsForLastNMonths("C001", 3);

        assertEquals(0, response.getTotalTransactions());
        assertEquals(0, response.getTotalRewardPoints().intValue());
    }

    @Test
    void testNegativeAmountThrowsException() {
        when(repository.findCustomerById("C001"))
                .thenReturn(Optional.of(customer));

        Transaction t = Transaction.builder()
                .transactionId("T1")
                .customerId("C001")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("-10"))
                .build();

        when(repository.findTransactionsByCustomerIdAndDateRange(any(), any(), any()))
                .thenReturn(Collections.singletonList(t));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateRewardsForLastNMonths("C001", 3));
    }

    // =========================
    // 🔧 HELPER METHOD
    // =========================

    private java.util.List<Transaction> mockTransactions() {
        return Arrays.asList(
                Transaction.builder()
                        .transactionId("T1")
                        .customerId("C001")
                        .transactionDate(LocalDate.now().minusDays(10))
                        .amount(new BigDecimal("120"))
                        .description("Test1")
                        .build(),

                Transaction.builder()
                        .transactionId("T2")
                        .customerId("C001")
                        .transactionDate(LocalDate.now().minusDays(5))
                        .amount(new BigDecimal("80"))
                        .description("Test2")
                        .build()
        );
    }
}