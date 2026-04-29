package com.retailer.rewards.controller;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.service.RewardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller exposing the Rewards Program endpoints.
 *
 * <h3>Base path</h3>
 * {@code /api/v1/transactions}
 *
 * <h3>Unified Endpoint</h3>
 * <ul>
 *   <li>{@code POST /api/v1/transactions} – handles all transaction reward queries</li>
 * </ul>
 *
 * <h3>Request Parameters</h3>
 * <ul>
 *   <li>{@code queryType} – "single" (customer rewards), "range" (date range), or "all" (all customers)</li>
 *   <li>{@code customerId} – (required for queryType=single and range)</li>
 *   <li>{@code months} – (optional, default=3, used with queryType=single or all)</li>
 *   <li>{@code startDate} – (required for queryType=range, format: YYYY-MM-DD)</li>
 *   <li>{@code endDate} – (required for queryType=range, format: YYYY-MM-DD)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/transactions")
public class RewardsController {

    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Unified endpoint for handling all transaction reward queries.
     * Routes based on queryType parameter to handle different query patterns.
     *
     * @param queryType query mode: "single" (customer), "range" (date range), or "all" (all customers)
     * @param customerId customer identifier (required for single and range queries)
     * @param months number of months to look back for single/all queries (default=3)
     * @param startDate query start date for range queries (ISO format: YYYY-MM-DD)
     * @param endDate query end date for range queries (ISO format: YYYY-MM-DD)
     * @return {@link RewardsResponse} or list of responses depending on queryType
     */
    @PostMapping
    public ResponseEntity<?> processTransactionQuery(
            @RequestParam("queryType") String queryType,
            @RequestParam(value = "customerId", required = false) String customerId,
            @RequestParam(value = "months", defaultValue = "3") int months,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        switch (queryType.toLowerCase()) {
            case "single":
                log.info("Processing single customer transaction query for customerId={}, months={}", customerId, months);
                RewardsResponse response = rewardsService.calculateRewardsForLastNMonths(customerId, months);
                return ResponseEntity.ok(response);

            case "range":
                log.info("Processing date range transaction query for customerId={}, startDate={}, endDate={}", 
                        customerId, startDate, endDate);
                RewardsResponse rangeResponse = rewardsService.calculateRewardsByDateRange(customerId, startDate, endDate);
                return ResponseEntity.ok(rangeResponse);

            case "all":
                log.info("Processing all customers transaction query for months={}", months);
                List<RewardsResponse> allResponses = rewardsService.calculateRewardsForAllCustomers(months);
                return ResponseEntity.ok(allResponses);

            default:
                log.warn("Invalid queryType parameter: {}", queryType);
                return ResponseEntity.badRequest().body(
                        "Invalid queryType. Must be 'single', 'range', or 'all'");
        }
    }
}
