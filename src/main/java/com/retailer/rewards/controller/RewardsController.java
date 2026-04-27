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
 * {@code /api/v1/rewards}
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code GET /api/v1/rewards/{customerId}}        – rewards for one customer (last N months)</li>
 *   <li>{@code GET /api/v1/rewards/{customerId}/range}  – rewards for one customer in a date range</li>
 *   <li>{@code GET /api/v1/rewards}                     – rewards for all customers (last N months)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/rewards")
public class RewardsController {

    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    /**
     * Retrieves reward points for a single customer over the last N months.
     *
     * @param customerId path variable – customer identifier
     * @param months     query param   – how many months to look back (default = 3, max = 36)
     * @return {@link RewardsResponse} with monthly and total reward breakdown
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<RewardsResponse> getRewardsByCustomer(
            @PathVariable String customerId,
            @RequestParam(value = "months", defaultValue = "3") int months) {

        log.info("GET /api/v1/rewards/{} ?months={}", customerId, months);

        RewardsResponse response = rewardsService.calculateRewardsForLastNMonths(customerId, months);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves reward points for a single customer within an explicit date range.
     *
     * @param customerId path variable – customer identifier
     * @param startDate  query param   – period start date (ISO format: YYYY-MM-DD)
     * @param endDate    query param   – period end date   (ISO format: YYYY-MM-DD)
     * @return {@link RewardsResponse} with monthly and total reward breakdown
     */
    @GetMapping("/{customerId}/range")
    public ResponseEntity<RewardsResponse> getRewardsByCustomerAndDateRange(
            @PathVariable String customerId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /api/v1/rewards/{}/range ?startDate={}&endDate={}", customerId, startDate, endDate);

        RewardsResponse response = rewardsService.calculateRewardsByDateRange(customerId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves reward points for <em>all</em> customers over the last N months.
     *
     * @param months query param – how many months to look back (default = 3, max = 36)
     * @return list of {@link RewardsResponse}, one per customer
     */
    @GetMapping
    public ResponseEntity<List<RewardsResponse>> getRewardsForAllCustomers(
            @RequestParam(value = "months", defaultValue = "3") int months) {

        log.info("GET /api/v1/rewards ?months={}", months);

        List<RewardsResponse> responses = rewardsService.calculateRewardsForAllCustomers(months);
        return ResponseEntity.ok(responses);
    }
}
