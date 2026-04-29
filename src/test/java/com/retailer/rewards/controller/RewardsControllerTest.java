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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc-based tests for {@link RewardsController}.
 * The service layer is fully mocked so only HTTP contract is verified here.
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
        // Wire up MockMvc with the global exception handler so 4xx/5xx are tested correctly
        mockMvc = MockMvcBuilders
                .standaloneSetup(rewardsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── POST /api/v1/transactions (queryType=single) ──────────────────────────

    @Nested
    @DisplayName("POST /api/v1/transactions (queryType=single)")
    class SingleCustomerQueryTests {

        @Test
        @DisplayName("200 OK – returns rewards for valid customer with default 3 months")
        void validCustomerDefaultMonths() throws Exception {
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", new BigDecimal("365"), 3);

            when(rewardsService.calculateRewardsForLastNMonths("C001", 3)).thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "single")
                            .param("customerId", "C001")
                            .accept(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.customerId",         is("C001")))
                    .andExpect(jsonPath("$.customerName",       is("Alice Johnson")))
                    .andExpect(jsonPath("$.totalRewardPoints",  is(365)))
                    .andExpect(jsonPath("$.monthsCovered",      is(3)));
        }

        @Test
        @DisplayName("200 OK – custom months parameter is forwarded to service")
        void customMonthsParam() throws Exception {
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", new BigDecimal("100"), 6);

            when(rewardsService.calculateRewardsForLastNMonths("C001", 6)).thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "single")
                            .param("customerId", "C001")
                            .param("months", "6")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monthsCovered", is(6)));
        }

        @Test
        @DisplayName("404 Not Found – unknown customer ID")
        void unknownCustomer() throws Exception {
            when(rewardsService.calculateRewardsForLastNMonths("UNKNOWN", 3))
                    .thenThrow(new CustomerNotFoundException("UNKNOWN"));

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "single")
                            .param("customerId", "UNKNOWN")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", containsString("UNKNOWN")));
        }

        @Test
        @DisplayName("400 Bad Request – months out of range")
        void monthsOutOfRange() throws Exception {
            when(rewardsService.calculateRewardsForLastNMonths("C001", 0))
                    .thenThrow(new InvalidDateRangeException("months must be between 1 and 36"));

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "single")
                            .param("customerId", "C001")
                            .param("months", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("Zero total rewards returned when no qualifying transactions")
        void zeroRewardsForCustomer() throws Exception {
            RewardsResponse response = buildSampleResponse("C002", "Bob Smith", BigDecimal.ZERO, 3);
            response.setMonthlyBreakdown(Collections.emptyList());
            response.setTotalTransactions(0);

            when(rewardsService.calculateRewardsForLastNMonths("C002", 3)).thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "single")
                            .param("customerId", "C002")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRewardPoints",  is(0)))
                    .andExpect(jsonPath("$.totalTransactions",  is(0)))
                    .andExpect(jsonPath("$.monthlyBreakdown",   hasSize(0)));
        }
    }

    // ── POST /api/v1/transactions (queryType=range) ───────────────────────────

    @Nested
    @DisplayName("POST /api/v1/transactions (queryType=range)")
    class DateRangeQueryTests {

        @Test
        @DisplayName("200 OK – valid date range with exact date matching")
        void validDateRange() throws Exception {
            LocalDate start = LocalDate.now().minusMonths(3);
            LocalDate end   = LocalDate.now();
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", new BigDecimal("200"), 3);

            when(rewardsService.calculateRewardsByDateRange(eq("C001"), eq(start), eq(end)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "range")
                            .param("customerId", "C001")
                            .param("startDate", start.toString())
                            .param("endDate",   end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRewardPoints", is(200)));
        }

        @Test
        @DisplayName("400 Bad Request – missing startDate parameter")
        void missingStartDate() throws Exception {
            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "range")
                            .param("customerId", "C001")
                            .param("endDate", LocalDate.now().toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request – missing endDate parameter")
        void missingEndDate() throws Exception {
            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "range")
                            .param("customerId", "C001")
                            .param("startDate", LocalDate.now().minusMonths(1).toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request – end before start with exact date assertion")
        void endBeforeStart() throws Exception {
            LocalDate start = LocalDate.now();
            LocalDate end   = LocalDate.now().minusDays(1);
            
            when(rewardsService.calculateRewardsByDateRange(eq("C001"), eq(start), eq(end)))
                    .thenThrow(new InvalidDateRangeException("endDate must not be before startDate"));

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "range")
                            .param("customerId", "C001")
                            .param("startDate", start.toString())
                            .param("endDate",   end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("endDate")));
        }

        @Test
        @DisplayName("404 Not Found – customer not found in date range query")
        void customerNotFoundInRange() throws Exception {
            LocalDate start = LocalDate.now().minusMonths(1);
            LocalDate end   = LocalDate.now();
            
            when(rewardsService.calculateRewardsByDateRange(eq("GHOST"), eq(start), eq(end)))
                    .thenThrow(new CustomerNotFoundException("GHOST"));

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "range")
                            .param("customerId", "GHOST")
                            .param("startDate", start.toString())
                            .param("endDate",   end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ── POST /api/v1/transactions (queryType=all) ────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/transactions (queryType=all)")
    class AllCustomersQueryTests {

        @Test
        @DisplayName("200 OK – returns list with one entry per customer")
        void allCustomersDefaultMonths() throws Exception {
            List<RewardsResponse> responses = Arrays.asList(
                    buildSampleResponse("C001", "Alice Johnson", new BigDecimal("365"), 3),
                    buildSampleResponse("C002", "Bob Smith",      new BigDecimal("90"), 3)
            );

            when(rewardsService.calculateRewardsForAllCustomers(3)).thenReturn(responses);

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "all")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].customerId", is("C001")))
                    .andExpect(jsonPath("$[1].customerId", is("C002")));
        }

        @Test
        @DisplayName("200 OK – empty list when no customers exist")
        void noCustomers() throws Exception {
            when(rewardsService.calculateRewardsForAllCustomers(3))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "all")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("400 Bad Request – invalid months param for all-customers endpoint")
        void invalidMonthsAllCustomers() throws Exception {
            when(rewardsService.calculateRewardsForAllCustomers(0))
                    .thenThrow(new InvalidDateRangeException("months must be between 1 and 36"));

            mockMvc.perform(post("/api/v1/transactions")
                            .param("queryType", "all")
                            .param("months", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private RewardsResponse buildSampleResponse(
            String customerId, String customerName, BigDecimal totalPoints, int months) {

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
