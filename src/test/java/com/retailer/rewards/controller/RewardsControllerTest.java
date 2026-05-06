package com.retailer.rewards.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static com.retailer.rewards.TestConstants.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDateRangeQuerySuccess() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(START_DATE_PARAM, LocalDate.now().minusMonths(DEFAULT_MONTHS).toString())
                        .param(END_DATE_PARAM, LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CUSTOMER_ID).value(CUSTOMER_ID))
                .andExpect(jsonPath(JSON_CUSTOMER_NAME).value(CUSTOMER_NAME))
                .andExpect(jsonPath(JSON_TOTAL_TRANSACTIONS).value(SEEDED_CUSTOMER_COUNT));
    }

    @Test
    void testSingleCustomerDefaultMonths() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_CUSTOMER_ID).value(CUSTOMER_ID))
                .andExpect(jsonPath(JSON_CUSTOMER_NAME).value(CUSTOMER_NAME))
                .andExpect(jsonPath(JSON_TOTAL_TRANSACTIONS).value(SEEDED_CUSTOMER_COUNT));
    }

    @Test
    void testAllCustomersDefaultMonths() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JSON_ROOT, hasSize(SEEDED_CUSTOMER_COUNT)));
    }

    @Test
    void testMonthsAndDateRangeConflict() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(MONTHS_PARAM, String.valueOf(CUSTOM_MONTHS))
                        .param(START_DATE_PARAM, LocalDate.now().toString())
                        .param(END_DATE_PARAM, LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingEndDate() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(START_DATE_PARAM, LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingStartDate() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(END_DATE_PARAM, LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDateRangeWithoutCustomerId() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(START_DATE_PARAM, RANGE_START.toString())
                        .param(END_DATE_PARAM, RANGE_END.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(START_DATE_PARAM, LATER_RANGE_START.toString())
                        .param(END_DATE_PARAM, EARLIER_RANGE_END.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFutureStartDate() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                        .param(START_DATE_PARAM, LocalDate.now().plusDays(1).toString())
                        .param(END_DATE_PARAM, LocalDate.now().plusDays(2).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidMonthsLow() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(MONTHS_PARAM, String.valueOf(MIN_INVALID_MONTHS)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidMonthsHigh() throws Exception {
        mockMvc.perform(get(REWARDS_API_PATH)
                        .param(MONTHS_PARAM, String.valueOf(MAX_INVALID_MONTHS)))
                .andExpect(status().isBadRequest());
    }
}
