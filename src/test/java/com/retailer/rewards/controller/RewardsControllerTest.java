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

    // ── GET /api/v1/rewards/{customerId} ─────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/rewards/{customerId}")
    class GetRewardsByCustomerTests {

        @Test
        @DisplayName("200 OK – returns rewards for valid customer with default 3 months")
        void validCustomerDefaultMonths() throws Exception {
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", 365L, 3);

            when(rewardsService.calculateRewardsForLastNMonths("C001", 3)).thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards/C001")
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
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", 100L, 6);

            when(rewardsService.calculateRewardsForLastNMonths("C001", 6)).thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards/C001")
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

            mockMvc.perform(get("/api/v1/rewards/UNKNOWN")
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

            mockMvc.perform(get("/api/v1/rewards/C001")
                            .param("months", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status", is(400)));
        }

        @Test
        @DisplayName("400 Bad Request – months is not a number")
        void monthsIsNotNumber() throws Exception {
            mockMvc.perform(get("/api/v1/rewards/C001")
                            .param("months", "abc")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Zero total rewards returned when no qualifying transactions")
        void zeroRewardsForCustomer() throws Exception {
            RewardsResponse response = buildSampleResponse("C002", "Bob Smith", 0L, 3);
            response.setMonthlyBreakdown(Collections.emptyList());
            response.setTotalTransactions(0);

            when(rewardsService.calculateRewardsForLastNMonths("C002", 3)).thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards/C002")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRewardPoints",  is(0)))
                    .andExpect(jsonPath("$.totalTransactions",  is(0)))
                    .andExpect(jsonPath("$.monthlyBreakdown",   hasSize(0)));
        }
    }

    // ── GET /api/v1/rewards/{customerId}/range ───────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/rewards/{customerId}/range")
    class GetRewardsByRangeTests {

        @Test
        @DisplayName("200 OK – valid date range")
        void validDateRange() throws Exception {
            LocalDate start = LocalDate.now().minusMonths(3);
            LocalDate end   = LocalDate.now();
            RewardsResponse response = buildSampleResponse("C001", "Alice Johnson", 200L, 3);

            when(rewardsService.calculateRewardsByDateRange(eq("C001"), any(), any()))
                    .thenReturn(response);

            mockMvc.perform(get("/api/v1/rewards/C001/range")
                            .param("startDate", start.toString())
                            .param("endDate",   end.toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRewardPoints", is(200)));
        }

        @Test
        @DisplayName("400 Bad Request – missing startDate parameter")
        void missingStartDate() throws Exception {
            mockMvc.perform(get("/api/v1/rewards/C001/range")
                            .param("endDate", LocalDate.now().toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request – missing endDate parameter")
        void missingEndDate() throws Exception {
            mockMvc.perform(get("/api/v1/rewards/C001/range")
                            .param("startDate", LocalDate.now().minusMonths(1).toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 Bad Request – end before start (service throws)")
        void endBeforeStart() throws Exception {
            when(rewardsService.calculateRewardsByDateRange(eq("C001"), any(), any()))
                    .thenThrow(new InvalidDateRangeException("endDate must not be before startDate"));

            mockMvc.perform(get("/api/v1/rewards/C001/range")
                            .param("startDate", LocalDate.now().toString())
                            .param("endDate",   LocalDate.now().minusDays(1).toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("endDate")));
        }

        @Test
        @DisplayName("404 Not Found – customer not found in date range query")
        void customerNotFoundInRange() throws Exception {
            when(rewardsService.calculateRewardsByDateRange(eq("GHOST"), any(), any()))
                    .thenThrow(new CustomerNotFoundException("GHOST"));

            mockMvc.perform(get("/api/v1/rewards/GHOST/range")
                            .param("startDate", LocalDate.now().minusMonths(1).toString())
                            .param("endDate",   LocalDate.now().toString())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ── GET /api/v1/rewards ───────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/rewards (all customers)")
    class GetAllCustomersRewardsTests {

        @Test
        @DisplayName("200 OK – returns list with one entry per customer")
        void allCustomersDefaultMonths() throws Exception {
            List<RewardsResponse> responses = Arrays.asList(
                    buildSampleResponse("C001", "Alice Johnson", 365L, 3),
                    buildSampleResponse("C002", "Bob Smith",      90L, 3)
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
        @DisplayName("200 OK – empty list when no customers exist")
        void noCustomers() throws Exception {
            when(rewardsService.calculateRewardsForAllCustomers(3))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/rewards")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("400 Bad Request – invalid months param for all-customers endpoint")
        void invalidMonthsAllCustomers() throws Exception {
            when(rewardsService.calculateRewardsForAllCustomers(0))
                    .thenThrow(new InvalidDateRangeException("months must be between 1 and 36"));

            mockMvc.perform(get("/api/v1/rewards")
                            .param("months", "0")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private RewardsResponse buildSampleResponse(
            String customerId, String customerName, long totalPoints, int months) {

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
