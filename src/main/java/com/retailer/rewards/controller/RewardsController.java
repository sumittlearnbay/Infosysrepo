package com.retailer.rewards.controller;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.InvalidDateRangeException;
import com.retailer.rewards.service.RewardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the Rewards Program API.
 * 
 * Single GET endpoint with parameter-driven routing:
 * - startDate + endDate present → date range query
 * - customerId present (no dates) → single customer, N months
 * - Neither customerId nor dates → all customers, N months
 */
@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);
    private static final int MAX_MONTHS = 36;
    private static final int MIN_MONTHS = 1;

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Unified GET endpoint for all reward queries.
     * Routes based on parameter combination:
     * 
     * 1. startDate + endDate present → date range query for customerId
     * 2. customerId present (no dates) → single customer, last N months
     * 3. Neither customerId nor dates → all customers, last N months
     *
     * @param customerId customer ID (optional)
     * @param months number of months to look back (1-36, default 3)
     * @param startDate query start date (ISO format: YYYY-MM-DD, optional)
     * @param endDate query end date (ISO format: YYYY-MM-DD, optional)
     * @return RewardsResponse or list of responses depending on query type
     */
    @GetMapping
    public ResponseEntity<?> getRewards(
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "months", defaultValue = "3") int months,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Validation: Conflicting parameters
        if (months != 3 && (startDate != null || endDate != null)) {
            throw new InvalidDateRangeException(
                    "Cannot specify both 'months' and date range parameters together");
        }

        // Validation: Partial date range
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            if (startDate == null) {
                throw new InvalidDateRangeException("'startDate' parameter is missing for date range query");
            } else {
                throw new InvalidDateRangeException("'endDate' parameter is missing for date range query");
            }
        }

        // Route 1: Date range query
        if (startDate != null && endDate != null) {
            return handleDateRangeQuery(customerId, startDate, endDate);
        }

        // Validation: Invalid months
        if (months < MIN_MONTHS || months > MAX_MONTHS) {
            throw new InvalidDateRangeException(
                    String.format("'months' must be between %d and %d", MIN_MONTHS, MAX_MONTHS));
        }

        // Route 2: Single customer (last N months)
        if (customerId != null && !customerId.trim().isEmpty()) {
            return handleSingleCustomerQuery(customerId, months);
        }

        // Route 3: All customers (last N months)
        return handleAllCustomersQuery(months);
    }

    /**
     * Handles date range query with all validations.
     */
    private ResponseEntity<RewardsResponse> handleDateRangeQuery(String customerId, LocalDate startDate, LocalDate endDate) {
        // Validation: Missing customerId for date range
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidDateRangeException(
                    "customerId is required for date range queries");
        }

        // Validation: End before start
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException(
                    "endDate cannot be before startDate");
        }

        // Validation: Future start date
        if (startDate.isAfter(LocalDate.now())) {
            throw new InvalidDateRangeException(
                    "startDate cannot be in the future");
        }

        log.info("Processing date range query for customerId={}, startDate={}, endDate={}", 
                customerId, startDate, endDate);

        RewardsResponse response = rewardsService.calculateRewardsByDateRange(customerId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles single customer query for last N months.
     */
    private ResponseEntity<RewardsResponse> handleSingleCustomerQuery(String customerId, int months) {
        log.info("Processing single customer query for customerId={}, months={}", customerId, months);

        RewardsResponse response = rewardsService.calculateRewardsForLastNMonths(customerId, months);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles all customers query for last N months.
     */
    private ResponseEntity<List<RewardsResponse>> handleAllCustomersQuery(int months) {
        log.info("Processing all customers query for months={}", months);

        List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(months);
        return ResponseEntity.ok(responses);
    }
}
