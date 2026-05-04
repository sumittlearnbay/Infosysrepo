package com.retailer.rewards.controller;

import com.retailer.rewards.dto.MonthlyRewardsSummary;
import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.GlobalExceptionHandler;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for RewardsController with parameter-driven routing.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RewardsController Tests")
class RewardsControllerTest {

    @Mock
    private RewardsService rewardsService;

    @InjectMocks
    private RewardsController rewardsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(rewardsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Single Customer Query Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("Single Customer Query (customerId present, no dates)")
    class SingleCustomerQueryTests {

        @Test
        @DisplayName("200 OK – single customer with default 3 months")
        void singleCustomerDefaultMonths() throws Exception {
            RewardsResponse response = buildResponse("C001", "Alice Johnson", new BigDecimal("365"), 3);
            when(rewardsService.calculateRewardsForLastNMonths("C001", 3)).thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId", is("C001")))
                    .andExpect(jsonPath("$.customerName", is("Alice Johnson")))
                    .andExpect(jsonPath("$.totalRewardPoints", is(365)));
        }

        @Test
        @DisplayName("200 OK – custom months parameter forwarded to service")
        void customMonthsParam() throws Exception {
            RewardsResponse response = buildResponse("C001", "Alice Johnson", new BigDecimal("100"), 6);
            when(rewardsService.calculateRewardsForLastNMonths("C001", 6)).thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("months", "6")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monthsCovered", is(6)));
        }

        @Test
        @DisplayName("404 Not Found – unknown customer")
        void unknownCustomer() throws Exception {
            when(rewardsService.calculateRewardsForLastNMonths("UNKNOWN", 3))
                    .thenThrow(new CustomerNotFoundException("UNKNOWN"));

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "UNKNOWN")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ── Date Range Query Tests ───────────────────────────────────────────────

    @Nested
    @DisplayName("Date Range Query (startDate + endDate present)")
    class DateRangeQueryTests {

        @Test
        @DisplayName("200 OK – valid date range with exact date matching")
        void validDateRange() throws Exception {
            LocalDate start = LocalDate.now().minusMonths(3);
            LocalDate end = LocalDate.now();
            RewardsResponse response = buildResponse("C001", "Alice Johnson", new BigDecimal("200"), 3);

            when(rewardsService.calculateRewardsByDateRange(eq("C001"), eq(start), eq(end)))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("startDate", start.toString())
                    .param("endDate", end.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRewardPoints", is(200)));
        }

        @Test
        @DisplayName("400 Bad Request – missing startDate")
        void missingStartDate() throws Exception {
            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("endDate", LocalDate.now().toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("startDate")));
        }

        @Test
        @DisplayName("400 Bad Request – missing endDate")
        void missingEndDate() throws Exception {
            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("startDate", LocalDate.now().toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("endDate")));
        }

        @Test
        @DisplayName("400 Bad Request – missing customerId for date range")
        void missingCustomerIdForRange() throws Exception {
            LocalDate start = LocalDate.now().minusMonths(1);
            LocalDate end = LocalDate.now();

            mockMvc.perform(get("/api/v1/rewards")
                    .param("startDate", start.toString())
                    .param("endDate", end.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("customerId")));
        }

        @Test
        @DisplayName("400 Bad Request – end before start")
        void endBeforeStart() throws Exception {
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().minusDays(1);

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("startDate", start.toString())
                    .param("endDate", end.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("endDate")));
        }

        @Test
        @DisplayName("400 Bad Request – future start date")
        void futureStartDate() throws Exception {
            LocalDate start = LocalDate.now().plusDays(1);
            LocalDate end = start.plusDays(10);

            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("startDate", start.toString())
                    .param("endDate", end.toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("future")));
        }
    }

    // ── All Customers Query Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("All Customers Query (neither customerId nor dates)")
    class AllCustomersQueryTests {

        @Test
        @DisplayName("200 OK – returns list of all customers")
        void allCustomers() throws Exception {
            List<RewardsResponse> responses = Arrays.asList(
                    buildResponse("C001", "Alice Johnson", new BigDecimal("365"), 3),
                    buildResponse("C002", "Bob Smith", new BigDecimal("90"), 3)
            );
            when(rewardsService.calculateRewardsForAllCustomers(3)).thenReturn(responses);

            mockMvc.perform(get("/api/v1/rewards")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].customerId", is("C001")))
                    .andExpect(jsonPath("$[1].customerId", is("C002")));
        }

        @Test
        @DisplayName("200 OK – empty list when no customers")
        void noCustomers() throws Exception {
            when(rewardsService.calculateRewardsForAllCustomers(3))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/rewards")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // ── Parameter Validation Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("Parameter Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("400 Bad Request – invalid months (too low)")
        void monthsTooLow() throws Exception {
            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("months", "0")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("between")));
        }

        @Test
        @DisplayName("400 Bad Request – invalid months (too high)")
        void monthsTooHigh() throws Exception {
            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("months", "37")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("between")));
        }

        @Test
        @DisplayName("400 Bad Request – conflicting months and dates")
        void conflictingParameters() throws Exception {
            mockMvc.perform(get("/api/v1/rewards")
                    .param("customerId", "C001")
                    .param("months", "6")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().toString())
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("cannot")));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private RewardsResponse buildResponse(String customerId, String customerName, BigDecimal totalPoints, int months) {
        return RewardsResponse.builder()
                .customerId(customerId)
                .customerName(customerName)
                .email(customerId.toLowerCase() + "@example.com")
                .membershipTier("GOLD")
                .periodStart(LocalDate.now().minusMonths(months))
                .periodEnd(LocalDate.now())
                .monthsCovered(months)
                .monthlyBreakdown(Collections.singletonList(
                        MonthlyRewardsSummary.builder()
                                .year(LocalDate.now().getYear())
                                .month(LocalDate.now().getMonthValue())
                                .monthName(LocalDate.now().getMonth().name())
                                .monthlyPoints(totalPoints)
                                .transactions(Collections.emptyList())
                                .build()))
                .totalTransactions(1)
                .totalRewardPoints(totalPoints)
                .build();
    }
}
