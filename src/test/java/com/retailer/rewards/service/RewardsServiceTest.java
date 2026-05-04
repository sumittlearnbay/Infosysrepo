package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RewardsService with BigDecimal precision and FLOOR rounding.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RewardsService Tests")
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .customerId("C001")
                .name("Alice Johnson")
                .email("alice@example.com")
                .membershipTier("GOLD")
                .build();
    }

    // ── BigDecimal Precision Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("BigDecimal Precision with FLOOR Rounding")
    class PrecisionTests {

        @Test
        @DisplayName("$50.50 → 0 points (FLOOR ignores cents)")
        void floorRounding_50_50() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("50.50"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            
            // $50.50 FLOOR to $50 → 0 points (not over $50 threshold)
            assertThat(response.getTotalRewardPoints()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$120.75 → 90 points (FLOOR to $120, then calculate)")
        void floorRounding_120_75() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("120.75"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            
            // $120.75 FLOOR to $120
            // ($120 - $100) × 2 = 20 × 2 = 40 points (above $100)
            // ($100 - $50) = 50 points (between $50-$100)
            // Total = 40 + 50 = 90 points
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("90"));
        }

        @Test
        @DisplayName("$99.99 → 49 points (FLOOR to $99)")
        void floorRounding_99_99() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("99.99"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            
            // $99.99 FLOOR to $99
            // ($99 - $50) × 1 = 49 points
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("49"));
        }
    }

    // ── Point Calculation Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("Point Calculation Rules")
    class CalculationTests {

        @Test
        @DisplayName("$0 → 0 points")
        void zeroDollars() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(BigDecimal.ZERO)
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            assertThat(response.getTotalRewardPoints()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$49.00 → 0 points (below $50 threshold)")
        void below50Threshold() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("49.00"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            assertThat(response.getTotalRewardPoints()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$75.00 → 25 points ($75 - $50 = $25 at 1pt)")
        void midTierAmount() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("75.00"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("25"));
        }

        @Test
        @DisplayName("$200.00 → 250 points ((200-100)×2 + 50×1)")
        void largeAmount() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.singletonList(
                            Transaction.builder()
                                    .transactionId("T001")
                                    .customerId("C001")
                                    .transactionDate(LocalDate.now())
                                    .amount(new BigDecimal("200.00"))
                                    .description("Test")
                                    .build()));

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            // (200 - 100) × 2 + 50 = 100 × 2 + 50 = 200 + 50 = 250
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("250"));
        }
    }

    // ── Multiple Transactions Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("Multiple Transactions")
    class MultipleTransactionsTests {

        @Test
        @DisplayName("Multiple transactions accumulate correctly")
        void multipleTransactions() {
            List<Transaction> transactions = Arrays.asList(
                    Transaction.builder()
                            .transactionId("T001")
                            .customerId("C001")
                            .transactionDate(LocalDate.now())
                            .amount(new BigDecimal("120.00"))
                            .description("T1")
                            .build(),
                    Transaction.builder()
                            .transactionId("T002")
                            .customerId("C001")
                            .transactionDate(LocalDate.now())
                            .amount(new BigDecimal("75.00"))
                            .description("T2")
                            .build(),
                    Transaction.builder()
                            .transactionId("T003")
                            .customerId("C001")
                            .transactionDate(LocalDate.now())
                            .amount(new BigDecimal("200.00"))
                            .description("T3")
                            .build()
            );

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(transactions);

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            
            // T1: $120 → 90 pts
            // T2: $75 → 25 pts
            // T3: $200 → 250 pts
            // Total: 365 pts
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("365"));
            assertThat(response.getTotalTransactions()).isEqualTo(3);
        }

        @Test
        @DisplayName("No transactions returns zero rewards")
        void noTransactions() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any()))
                    .thenReturn(Collections.emptyList());

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            
            assertThat(response.getTotalRewardPoints()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.getTotalTransactions()).isZero();
            assertThat(response.getMonthlyBreakdown()).isEmpty();
        }
    }

    // ── Exception Tests ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionTests {

        @Test
        @DisplayName("CustomerNotFoundException thrown for unknown customer")
        void unknownCustomer() {
            when(transactionRepository.findCustomerById("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rewardsService.calculateRewardsForLastNMonths("UNKNOWN", 3))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }
    }
}
