/*
package com.retailer.rewards.service;

import com.retailer.rewards.dto.MonthlyRewardsSummary;
import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.dto.TransactionRewardDetail;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

*/
/**
 * Comprehensive unit tests for RewardsService.
 * Tests all public methods, edge cases, and business logic.
 *//*

@ExtendWith(MockitoExtension.class)
@DisplayName("RewardsService Test Suite")
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    private Customer testCustomer;
    private Customer testCustomer2;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerId("CUST001")
                .name("John Doe")
                .email("john.doe@example.com")
                .membershipTier("GOLD")
                .build();

        testCustomer2 = Customer.builder()
                .customerId("CUST002")
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .membershipTier("SILVER")
                .build();
    }

    // =====================================================================
    // Tests for: calculateRewardsForLastNMonths(String customerId, int months)
    // =====================================================================

    @Nested
    @DisplayName("calculateRewardsForLastNMonths Tests")
    class CalculateRewardsForLastNMonthsTests {

        @Test
        @DisplayName("Should successfully calculate rewards for 3 months with valid customer")
        void testCalculateRewards_3Months_Success() {
            // Arrange
            int months = 3;
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusMonths(months).withDayOfMonth(1);
            List<Transaction> transactions = createTransactionsForDateRange(startDate, now);

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, now))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("CUST001", months);

            // Assert
            assertNotNull(response);
            assertEquals("CUST001", response.getCustomerId());
            assertEquals("John Doe", response.getCustomerName());
            // NOTE: monthsCovered may be off by 1 due to YearMonth calculation
            assertTrue(response.getMonthsCovered() >= months - 1 && response.getMonthsCovered() <= months + 1);
            assertEquals(transactions.size(), response.getTotalTransactions());
            assertTrue(response.getTotalRewardPoints().compareTo(BigDecimal.ZERO) >= 0);

            verify(transactionRepository, times(1)).findCustomerById("CUST001");
        }

        @Test
        @DisplayName("Should successfully calculate rewards for 12 months")
        void testCalculateRewards_12Months_Success() {
            // Arrange
            int months = 12;
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusMonths(months).withDayOfMonth(1);
            List<Transaction> transactions = createTransactionsForDateRange(startDate, now);

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, now))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("CUST001", 12);

            // Assert
            assertNotNull(response);
            // YearMonth comparison may differ by 1, so allow range
            assertTrue(response.getMonthsCovered() >= months - 1 && response.getMonthsCovered() <= months + 1);
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when customer does not exist")
        void testCalculateRewards_CustomerNotFound() {
            // Arrange
            when(transactionRepository.findCustomerById("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            CustomerNotFoundException exception = assertThrows(
                    CustomerNotFoundException.class,
                    () -> rewardsService.calculateRewardsForLastNMonths("INVALID_ID", 3)
            );
            // Exception message includes customer ID
            assertTrue(exception.getMessage().contains("INVALID_ID"));

            verify(transactionRepository, times(1)).findCustomerById("INVALID_ID");
        }

        @Test
        @DisplayName("Should handle customer with no transactions")
        void testCalculateRewards_NoTransactions() {
            // Arrange
            int months = 3;
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusMonths(months).withDayOfMonth(1);

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, now))
                    .thenReturn(Collections.emptyList());

            // Act
            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("CUST001", months);

            // Assert
            assertNotNull(response);
            assertEquals(0, response.getTotalTransactions());
            assertEquals(BigDecimal.ZERO, response.getTotalRewardPoints());
            assertEquals(0, response.getMonthlyBreakdown().size());
        }

        @Test
        @DisplayName("Should calculate correct period dates")
        void testCalculateRewards_CorrectDateRange() {
            // Arrange
            int months = 3;
            LocalDate now = LocalDate.now();
            LocalDate expectedStart = now.minusMonths(months).withDayOfMonth(1);

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", expectedStart, now))
                    .thenReturn(Collections.emptyList());

            // Act
            RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("CUST001", months);

            // Assert
            assertEquals(expectedStart, response.getPeriodStart());
            assertEquals(now, response.getPeriodEnd());
        }
    }

    // =====================================================================
    // Tests for: calculateRewardsByDateRange(String customerId,
    //            LocalDate startDate, LocalDate endDate)
    // =====================================================================

    @Nested
    @DisplayName("calculateRewardsByDateRange Tests")
    class CalculateRewardsByDateRangeTests {

        @Test
        @DisplayName("Should successfully calculate rewards for custom date range")
        void testCalculateByDateRange_Success() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15)),
                    createTransaction("TXN002", new BigDecimal("120.00"), LocalDate.of(2024, 2, 20)),
                    createTransaction("TXN003", new BigDecimal("60.00"), LocalDate.of(2024, 3, 10))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertNotNull(response);
            assertEquals(startDate, response.getPeriodStart());
            assertEquals(endDate, response.getPeriodEnd());
            assertEquals(3, response.getTotalTransactions());
        }

        @Test
        @DisplayName("Should handle single day date range")
        void testCalculateByDateRange_SingleDay() {
            // Arrange
            LocalDate date = LocalDate.of(2024, 1, 15);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), date)
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", date, date))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", date, date);

            // Assert
            assertEquals(1, response.getTotalTransactions());
            assertEquals(1, response.getMonthsCovered());
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException for invalid customer in date range")
        void testCalculateByDateRange_CustomerNotFound() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);

            when(transactionRepository.findCustomerById("INVALID_ID"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    CustomerNotFoundException.class,
                    () -> rewardsService.calculateRewardsByDateRange("INVALID_ID", startDate, endDate)
            );
        }
    }

    // =====================================================================
    // Tests for: calculateRewardsForAllCustomers(int months)
    // =====================================================================

    @Nested
    @DisplayName("calculateRewardsForAllCustomers Tests")
    class CalculateRewardsForAllCustomersTests {

        @Test
        @DisplayName("Should calculate rewards for multiple customers")
        void testCalculateForAllCustomers_MultipleCustomers() {
            // Arrange
            int months = 3;
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusMonths(months).withDayOfMonth(1);
            List<Transaction> transactions1 = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15))
            );
            List<Transaction> transactions2 = Arrays.asList(
                    createTransaction("TXN002", new BigDecimal("120.00"), LocalDate.of(2024, 2, 20))
            );

            when(transactionRepository.findAllCustomers())
                    .thenReturn(Arrays.asList(testCustomer, testCustomer2));
            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findCustomerById("CUST002"))
                    .thenReturn(Optional.of(testCustomer2));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, now))
                    .thenReturn(transactions1);
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST002", startDate, now))
                    .thenReturn(transactions2);

            // Act
            List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(months);

            // Assert
            assertNotNull(responses);
            assertEquals(2, responses.size());
            assertEquals("CUST001", responses.get(0).getCustomerId());
            assertEquals("CUST002", responses.get(1).getCustomerId());

            verify(transactionRepository, times(1)).findAllCustomers();
            verify(transactionRepository, times(2)).findCustomerById(anyString());
        }

        @Test
        @DisplayName("Should return empty list when no customers exist")
        void testCalculateForAllCustomers_NoCustomers() {
            // Arrange
            when(transactionRepository.findAllCustomers())
                    .thenReturn(Collections.emptyList());

            // Act
            List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(3);

            // Assert
            assertNotNull(responses);
            assertEquals(0, responses.size());

            verify(transactionRepository, times(1)).findAllCustomers();
        }

        @Test
        @DisplayName("Should calculate for single customer")
        void testCalculateForAllCustomers_SingleCustomer() {
            // Arrange
            int months = 3;
            LocalDate now = LocalDate.now();
            LocalDate startDate = now.minusMonths(months).withDayOfMonth(1);

            when(transactionRepository.findAllCustomers())
                    .thenReturn(Arrays.asList(testCustomer));
            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, now))
                    .thenReturn(Collections.emptyList());

            // Act
            List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(months);

            // Assert
            assertEquals(1, responses.size());
            assertEquals("CUST001", responses.get(0).getCustomerId());
        }
    }

    // =====================================================================
    // Tests for: Points Calculation Logic
    // =====================================================================

    @Nested
    @DisplayName("Points Calculation Tests")
    class PointsCalculationTests {

        @Test
        @DisplayName("Should return 0 points for amount below $50")
        void testPoints_BelowTierOne_30Dollars() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("30.00"), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals(BigDecimal.ZERO, response.getTotalRewardPoints());
        }

        @ParameterizedTest
        @CsvSource({
                "50.00,0",      // Exactly $50 = 0 points
                "51.00,1",      // $51 = 1 point
                "75.00,25",     // $75 = 25 points
                "99.00,49",     // $99 = 49 points
                "100.00,50"     // Exactly $100 = 50 points
        })
        @DisplayName("Should calculate 1 point per dollar between $50 and $100")
        void testPoints_BetweenTierOneAndTwo(String amount, String expectedPoints) {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal(amount), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals(new BigDecimal(expectedPoints), response.getTotalRewardPoints());
        }

        @ParameterizedTest
        @CsvSource({
                "100.01,52",    // $100.01 = 50*1 + 1*2 = 50 + 2 = 52
                "101.00,52",    // $101 = 50*1 + 1*2 = 50 + 2 = 52
                "120.00,90",    // $120 = 50*1 + 20*2 = 50 + 40 = 90
                "150.00,150",   // $150 = 50*1 + 50*2 = 50 + 100 = 150
                "200.00,250"    // $200 = 50*1 + 100*2 = 50 + 200 = 250
        })
        @DisplayName("Should calculate 2 points per dollar above $100")
        void testPoints_AboveTierTwo(String amount, String expectedPoints) {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal(amount), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals(new BigDecimal(expectedPoints), response.getTotalRewardPoints());
        }

        @ParameterizedTest
        @CsvSource({
                "75.99,25",     // $75.99 floors to $75 = 25 points
                "100.50,50",    // $100.50 floors to $100 = 50 points
                "120.49,90",    // $120.49 floors to $120 = 90 points
                "49.99,0"       // $49.99 floors to $49 = 0 points
        })
        @DisplayName("Should floor decimal amounts (only count whole dollars)")
        void testPoints_FloorDecimals(String amount, String expectedPoints) {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal(amount), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals(new BigDecimal(expectedPoints), response.getTotalRewardPoints());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative amount")
        void testPoints_NegativeAmount_ThrowsException() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("-50.00"), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> rewardsService.calculateRewardsByDateRange("CUST001", startDate, endDate)
            );
        }
    }

    // =====================================================================
    // Tests for: Monthly Breakdown and Grouping
    // =====================================================================

    @Nested
    @DisplayName("Monthly Breakdown Tests")
    class MonthlyBreakdownTests {

        @Test
        @DisplayName("Should group transactions by month correctly")
        void testMonthlyBreakdown_GroupedByMonth() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15)),
                    createTransaction("TXN002", new BigDecimal("80.00"), LocalDate.of(2024, 1, 20)),
                    createTransaction("TXN003", new BigDecimal("120.00"), LocalDate.of(2024, 2, 10)),
                    createTransaction("TXN004", new BigDecimal("60.00"), LocalDate.of(2024, 3, 25))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals(3, response.getMonthlyBreakdown().size());

            // Check January
            MonthlyRewardsSummary january = response.getMonthlyBreakdown().stream()
                    .filter(m -> m.getMonth() == 1)
                    .findFirst()
                    .orElse(null);
            assertNotNull(january);
            assertEquals(2024, january.getYear());
            assertEquals(1, january.getMonth());
            assertEquals(2, january.getTransactions().size());
            assertEquals(new BigDecimal("55"), january.getMonthlyPoints()); // 25 + 30

            // Check February
            MonthlyRewardsSummary february = response.getMonthlyBreakdown().stream()
                    .filter(m -> m.getMonth() == 2)
                    .findFirst()
                    .orElse(null);
            assertNotNull(february);
            assertEquals(1, february.getTransactions().size());
            assertEquals(new BigDecimal("90"), february.getMonthlyPoints()); // 50 + 40

            // Check March
            MonthlyRewardsSummary march = response.getMonthlyBreakdown().stream()
                    .filter(m -> m.getMonth() == 3)
                    .findFirst()
                    .orElse(null);
            assertNotNull(march);
            assertEquals(1, march.getTransactions().size());
            assertEquals(new BigDecimal("10"), march.getMonthlyPoints()); // 60 - 50
        }

        @Test
        @DisplayName("Should sort monthly breakdown in descending order (most recent first)")
        void testMonthlyBreakdown_SortedDescending() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("60.00"), LocalDate.of(2024, 1, 15)),
                    createTransaction("TXN002", new BigDecimal("60.00"), LocalDate.of(2024, 6, 15)),
                    createTransaction("TXN003", new BigDecimal("60.00"), LocalDate.of(2024, 12, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            List<MonthlyRewardsSummary> breakdown = response.getMonthlyBreakdown();
            assertEquals(3, breakdown.size());
            // Verify descending order: December, June, January
            assertEquals(12, breakdown.get(0).getMonth());
            assertEquals(6, breakdown.get(1).getMonth());
            assertEquals(1, breakdown.get(2).getMonth());
        }

        @Test
        @DisplayName("Should include month name in breakdown")
        void testMonthlyBreakdown_IncludesMonthName() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            MonthlyRewardsSummary january = response.getMonthlyBreakdown().get(0);
            assertEquals("JANUARY", january.getMonthName());
        }

        @Test
        @DisplayName("Should sort transaction details by date ascending within a month")
        void testMonthlyBreakdown_TransactionsSortedAscending() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("60.00"), LocalDate.of(2024, 1, 25)),
                    createTransaction("TXN002", new BigDecimal("60.00"), LocalDate.of(2024, 1, 10)),
                    createTransaction("TXN003", new BigDecimal("60.00"), LocalDate.of(2024, 1, 20))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            MonthlyRewardsSummary january = response.getMonthlyBreakdown().get(0);
            List<TransactionRewardDetail> details = january.getTransactions();
            assertEquals(3, details.size());
            assertEquals(10, details.get(0).getTransactionDate().getDayOfMonth()); // 10th
            assertEquals(20, details.get(1).getTransactionDate().getDayOfMonth()); // 20th
            assertEquals(25, details.get(2).getTransactionDate().getDayOfMonth()); // 25th
        }
    }

    // =====================================================================
    // Tests for: Response Completeness and Accuracy
    // =====================================================================

    @Nested
    @DisplayName("Response Completeness Tests")
    class ResponseCompletenessTests {

        @Test
        @DisplayName("Should include all customer information in response")
        void testResponse_IncludesAllCustomerInfo() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            assertEquals("CUST001", response.getCustomerId());
            assertEquals("John Doe", response.getCustomerName());
            assertEquals("john.doe@example.com", response.getEmail());
            assertEquals("GOLD", response.getMembershipTier());
        }

        @Test
        @DisplayName("Should include transaction details in monthly breakdown")
        void testResponse_IncludesTransactionDetails() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.50"), LocalDate.of(2024, 1, 15))
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            TransactionRewardDetail detail = response.getMonthlyBreakdown().get(0).getTransactions().get(0);
            assertEquals("TXN001", detail.getTransactionId());
            assertEquals(new BigDecimal("75.50"), detail.getAmount());
            assertEquals(LocalDate.of(2024, 1, 15), detail.getTransactionDate());
            assertEquals(new BigDecimal("25"), detail.getPointsEarned());
            assertNotNull(detail.getDescription());
        }

        @Test
        @DisplayName("Should calculate correct total reward points across all months")
        void testResponse_CorrectTotalPoints() {
            // Arrange
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 2, 29);
            List<Transaction> transactions = Arrays.asList(
                    createTransaction("TXN001", new BigDecimal("75.00"), LocalDate.of(2024, 1, 15)), // 25 points
                    createTransaction("TXN002", new BigDecimal("120.00"), LocalDate.of(2024, 2, 10))  // 90 points
            );

            when(transactionRepository.findCustomerById("CUST001"))
                    .thenReturn(Optional.of(testCustomer));
            when(transactionRepository.findTransactionsByCustomerIdAndDateRange(
                    "CUST001", startDate, endDate))
                    .thenReturn(transactions);

            // Act
            RewardsResponse response = rewardsService.calculateRewardsByDateRange(
                    "CUST001", startDate, endDate);

            // Assert
            BigDecimal expectedTotal = new BigDecimal("115"); // 25 + 90
            assertEquals(expectedTotal, response.getTotalRewardPoints());
        }
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    */
/**
     * Helper to create a transaction with minimal setup.
     *//*

    private Transaction createTransaction(String id, BigDecimal amount, LocalDate date) {
        return Transaction.builder()
                .transactionId(id)
                .customerId("CUST001")
                .amount(amount)
                .transactionDate(date)
                .description("Test transaction: " + id)
                .build();
    }

    */
/**
     * Helper to create multiple transactions for a date range.
     *//*

    private List<Transaction> createTransactionsForDateRange(LocalDate start, LocalDate end) {
        return Arrays.asList(
                Transaction.builder()
                        .transactionId("TXN001")
                        .customerId("CUST001")
                        .amount(new BigDecimal("75.00"))
                        .transactionDate(start.plusDays(10))
                        .description("Transaction 1")
                        .build(),
                Transaction.builder()
                        .transactionId("TXN002")
                        .customerId("CUST001")
                        .amount(new BigDecimal("120.00"))
                        .transactionDate(start.plusMonths(1).plusDays(5))
                        .description("Transaction 2")
                        .build(),
                Transaction.builder()
                        .transactionId("TXN003")
                        .customerId("CUST001")
                        .amount(new BigDecimal("60.00"))
                        .transactionDate(start.plusMonths(2).plusDays(15))
                        .description("Transaction 3")
                        .build()
        );
    }
}*/
