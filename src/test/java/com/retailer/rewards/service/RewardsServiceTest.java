package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.InvalidDateRangeException;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewardsService}.
 * Uses Mockito to isolate the service from the data layer.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RewardsService Unit Tests")
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
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice@example.com")
                .membershipTier("GOLD")
                .build();
    }

    // ── calculatePoints ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("calculatePoints()")
    class CalculatePointsTests {

        @Test
        @DisplayName("$0 → 0 points")
        void zeroDollars() {
            assertThat(rewardsService.calculatePoints(BigDecimal.ZERO)).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$49.99 → 0 points (below $50 threshold)")
        void belowFiftyThreshold() {
            assertThat(rewardsService.calculatePoints(new BigDecimal("49.99"))).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$50.00 → 0 points (exactly at lower boundary, exclusive)")
        void exactlyFifty() {
            assertThat(rewardsService.calculatePoints(new BigDecimal("50.00"))).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$50.50 → 0 points (cents below $51)")
        void fiftyFiftyCents() {
            // Only whole dollars are counted – $50.50 earns 0 points (floor to $50, not over)
            assertThat(rewardsService.calculatePoints(new BigDecimal("50.50"))).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("$75.00 → 25 points (1 pt per dollar between $50–$100)")
        void seventyFiveDollars() {
            // $75 - $50 = $25 → 25 points
            assertThat(rewardsService.calculatePoints(new BigDecimal("75.00"))).isEqualTo(new BigDecimal("25"));
        }

        @Test
        @DisplayName("$100.00 → 50 points (exactly at second boundary)")
        void exactlyHundred() {
            // $100 - $50 = $50 at 1 pt = 50 points; nothing above $100
            assertThat(rewardsService.calculatePoints(new BigDecimal("100.00"))).isEqualTo(new BigDecimal("50"));
        }

        @Test
        @DisplayName("$120.00 → 90 points (example from spec)")
        void specExample() {
            // (2 × $20) + (1 × $50) = 40 + 50 = 90
            assertThat(rewardsService.calculatePoints(new BigDecimal("120.00"))).isEqualTo(new BigDecimal("90"));
        }

        @Test
        @DisplayName("$200.00 → 250 points")
        void twoHundredDollars() {
            // (2 × $100) + (1 × $50) = 200 + 50 = 250
            assertThat(rewardsService.calculatePoints(new BigDecimal("200.00"))).isEqualTo(new BigDecimal("250"));
        }

        @Test
        @DisplayName("$500.00 → 850 points")
        void fiveHundredDollars() {
            // (2 × $400) + (1 × $50) = 800 + 50 = 850
            assertThat(rewardsService.calculatePoints(new BigDecimal("500.00"))).isEqualTo(new BigDecimal("850"));
        }

        @Test
        @DisplayName("Null amount throws IllegalArgumentException")
        void nullAmountThrows() {
            assertThatThrownBy(() -> rewardsService.calculatePoints(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("Negative amount throws IllegalArgumentException")
        void negativeAmountThrows() {
            assertThatThrownBy(() -> rewardsService.calculatePoints(new BigDecimal("-10")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── calculateRewardsForLastNMonths ────────────────────────────────────────

    @Nested
    @DisplayName("calculateRewardsForLastNMonths()")
    class LastNMonthsTests {

        @Test
        @DisplayName("Returns correct total points for 3-month period")
        void threeMonthsRewards() {
            LocalDate now = LocalDate.now();
            List<Transaction> txns = Arrays.asList(
                    buildTx("T1", "C001", "120.00", now.withDayOfMonth(5)),
                    buildTx("T2", "C001", "75.00",  now.minusMonths(1).withDayOfMonth(10)),
                    buildTx("T3", "C001", "200.00", now.minusMonths(2).withDayOfMonth(15))
            );

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(txns);

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);

            // 120 → 90 pts; 75 → 25 pts; 200 → 250 pts; total = 365
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("365"));
            assertThat(response.getTotalTransactions()).isEqualTo(3);
            assertThat(response.getCustomerId()).isEqualTo("C001");
            assertThat(response.getMonthsCovered()).isEqualTo(3);
        }

        @Test
        @DisplayName("Customer not found throws CustomerNotFoundException")
        void customerNotFound() {
            when(transactionRepository.findCustomerById("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rewardsService.calculateRewardsForLastNMonths("UNKNOWN", 3))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }

        @Test
        @DisplayName("months=0 throws InvalidDateRangeException")
        void zeroMonthsThrows() {
            assertThatThrownBy(() -> rewardsService.calculateRewardsForLastNMonths("C001", 0))
                    .isInstanceOf(InvalidDateRangeException.class);
        }

        @Test
        @DisplayName("months=37 throws InvalidDateRangeException")
        void tooManyMonthsThrows() {
            assertThatThrownBy(() -> rewardsService.calculateRewardsForLastNMonths("C001", 37))
                    .isInstanceOf(InvalidDateRangeException.class);
        }

        @Test
        @DisplayName("months=1 is valid")
        void oneMonthValid() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(Collections.emptyList());

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 1);
            assertThat(response.getTotalRewardPoints()).isEqualTo(0L);
        }

        @Test
        @DisplayName("months=36 is valid (upper boundary)")
        void thirtysSixMonthsValid() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(Collections.emptyList());

            assertThatNoException().isThrownBy(
                    () -> rewardsService.calculateRewardsForLastNMonths("C001", 36));
        }

        @Test
        @DisplayName("No transactions returns zero reward points")
        void noTransactionsReturnsZero() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(Collections.emptyList());

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            assertThat(response.getTotalRewardPoints()).isZero();
            assertThat(response.getTotalTransactions()).isZero();
            assertThat(response.getMonthlyBreakdown()).isEmpty();
        }

        @Test
        @DisplayName("Response contains correct customer metadata")
        void responseContainsCustomerMetadata() {
            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(Collections.emptyList());

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            assertThat(response.getCustomerName()).isEqualTo("Alice Johnson");
            assertThat(response.getEmail()).isEqualTo("alice@example.com");
            assertThat(response.getMembershipTier()).isEqualTo("GOLD");
        }
    }

    // ── calculateRewardsByDateRange ───────────────────────────────────────────

    @Nested
    @DisplayName("calculateRewardsByDateRange()")
    class DateRangeTests {

        @Test
        @DisplayName("Valid range returns rewards correctly")
        void validRangeReturnsRewards() {
            LocalDate start = LocalDate.now().minusMonths(2);
            LocalDate end   = LocalDate.now();

            List<Transaction> txns = Collections.singletonList(
                    buildTx("T1", "C001", "120.00", LocalDate.now().withDayOfMonth(1)));

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(txns);

            RewardsResponse response = rewardsService.calculateRewardsByDateRange("C001", start, end);
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("90")); // $120 → 90 pts
        }

        @Test
        @DisplayName("End date before start date throws InvalidDateRangeException")
        void endBeforeStartThrows() {
            LocalDate start = LocalDate.now();
            LocalDate end   = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> rewardsService.calculateRewardsByDateRange("C001", start, end))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("endDate");
        }

        @Test
        @DisplayName("Future start date throws InvalidDateRangeException")
        void futureStartDateThrows() {
            LocalDate start = LocalDate.now().plusDays(1);
            LocalDate end   = LocalDate.now().plusMonths(1);

            assertThatThrownBy(() -> rewardsService.calculateRewardsByDateRange("C001", start, end))
                    .isInstanceOf(InvalidDateRangeException.class)
                    .hasMessageContaining("future");
        }

        @Test
        @DisplayName("Same start and end date (single day) is valid")
        void singleDayRangeIsValid() {
            LocalDate today = LocalDate.now();

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(Collections.emptyList());

            assertThatNoException().isThrownBy(
                    () -> rewardsService.calculateRewardsByDateRange("C001", today, today));
        }
    }

    // ── calculateRewardsForAllCustomers ───────────────────────────────────────

    @Nested
    @DisplayName("calculateRewardsForAllCustomers()")
    class AllCustomersTests {

        @Test
        @DisplayName("Returns one result per customer")
        void returnsOneResultPerCustomer() {
            Customer c2 = Customer.builder().customerId("C002")
                    .firstName("Bob").lastName("Smith")
                    .email("bob@example.com").membershipTier("SILVER").build();

            when(transactionRepository.findAllCustomers())
                    .thenReturn(Arrays.asList(sampleCustomer, c2));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    anyString(), any(), any())).thenReturn(Collections.emptyList());

            List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(3);
            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("months=0 throws InvalidDateRangeException")
        void invalidMonthsThrows() {
            assertThatThrownBy(() -> rewardsService.calculateRewardsForAllCustomers(0))
                    .isInstanceOf(InvalidDateRangeException.class);
        }

        @Test
        @DisplayName("Empty customer list returns empty response list")
        void noCustomersReturnsEmpty() {
            when(transactionRepository.findAllCustomers()).thenReturn(Collections.emptyList());

            List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(3);
            assertThat(responses).isEmpty();
        }
    }

    // ── Edge cases / boundary tests ──────────────────────────────────────────

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Transactions all below $50 earn zero points")
        void allTransactionsBelowThreshold() {
            LocalDate now = LocalDate.now();
            List<Transaction> txns = Arrays.asList(
                    buildTx("T1", "C001", "10.00", now),
                    buildTx("T2", "C001", "25.00", now),
                    buildTx("T3", "C001", "49.99", now)
            );

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(txns);

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 1);
            assertThat(response.getTotalRewardPoints()).isZero();
        }

        @Test
        @DisplayName("Transactions spanning multiple months are grouped correctly")
        void multiMonthGrouping() {
            LocalDate now = LocalDate.now();
            List<Transaction> txns = Arrays.asList(
                    buildTx("T1", "C001", "120.00", now.withDayOfMonth(1)),
                    buildTx("T2", "C001", "120.00", now.minusMonths(1).withDayOfMonth(1))
            );

            when(transactionRepository.findCustomerById("C001")).thenReturn(Optional.of(sampleCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    eq("C001"), any(), any())).thenReturn(txns);

            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);
            // Two different months → two monthly entries, 90 pts each
            assertThat(response.getMonthlyBreakdown()).hasSize(2);
            assertThat(response.getTotalRewardPoints()).isEqualTo(new BigDecimal("180"));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Transaction buildTx(String id, String custId, String amount, LocalDate date) {
        return Transaction.builder()
                .transactionId(id)
                .customerId(custId)
                .amount(new BigDecimal(amount))
                .transactionDate(date)
                .description("Test purchase")
                .build();
    }
}
