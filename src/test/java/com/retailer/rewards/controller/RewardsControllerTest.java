package com.retailer.rewards.controller;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardsControllerTest {

    @Mock
    private RewardsService rewardsService;

    @InjectMocks
    private RewardsController rewardsController;

    // =========================
    // ✅ HAPPY PATH TESTS
    // =========================

    @Test
    void testDateRangeQuerySuccess() {
        RewardsResponse mockResponse = new RewardsResponse();
        when(rewardsService.calculateRewardsByDateRange(anyString(), any(), any()))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = rewardsController.getRewards(
                "C1",
                3,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 2, 1)
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(rewardsService, times(1))
                .calculateRewardsByDateRange(anyString(), any(), any());
    }

    @Test
    void testSingleCustomerDefaultMonths() {
        RewardsResponse mockResponse = new RewardsResponse();
        when(rewardsService.calculateRewardsForLastNMonths("C1", 3))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = rewardsController.getRewards(
                "C1",
                3,
                null,
                null
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockResponse, response.getBody());

        verify(rewardsService).calculateRewardsForLastNMonths("C1", 3);
    }

    @Test
    void testAllCustomersDefaultMonths() {
        List<RewardsResponse> mockList = Arrays.asList(new RewardsResponse(), new RewardsResponse());

        when(rewardsService.calculateRewardsForAllCustomers(3))
                .thenReturn(mockList);

        ResponseEntity<?> response = rewardsController.getRewards(
                null,
                3,
                null,
                null
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockList, response.getBody());

        verify(rewardsService).calculateRewardsForAllCustomers(3);
    }

    // =========================
    // ❌ VALIDATION TESTS
    // =========================

    @Test
    void testMonthsAndDateRangeConflict() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        "C1",
                        5,
                        LocalDate.now(),
                        LocalDate.now()
                )
        );
    }

    @Test
    void testMissingEndDate() {
        InvalidDateRangeException ex = assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        "C1",
                        3,
                        LocalDate.now(),
                        null
                )
        );

        assertTrue(ex.getMessage().contains("endDate"));
    }

    @Test
    void testMissingStartDate() {
        InvalidDateRangeException ex = assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        "C1",
                        3,
                        null,
                        LocalDate.now()
                )
        );

        assertTrue(ex.getMessage().contains("startDate"));
    }

    @Test
    void testDateRangeWithoutCustomerId() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        null,
                        3,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 2, 1)
                )
        );
    }

    @Test
    void testEndDateBeforeStartDate() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        "C1",
                        3,
                        LocalDate.of(2024, 2, 1),
                        LocalDate.of(2024, 1, 1)
                )
        );
    }

    @Test
    void testFutureStartDate() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(
                        "C1",
                        3,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(2)
                )
        );
    }

    @Test
    void testInvalidMonthsLow() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(null, 0, null, null)
        );
    }

    @Test
    void testInvalidMonthsHigh() {
        assertThrows(InvalidDateRangeException.class, () ->
                rewardsController.getRewards(null, 37, null, null)
        );
    }
}