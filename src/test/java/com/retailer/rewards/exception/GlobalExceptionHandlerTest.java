package com.retailer.rewards.exception;

import com.retailer.rewards.controller.RewardsController;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies the error shapes produced by {@link GlobalExceptionHandler}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

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

    @Test
    @DisplayName("CustomerNotFoundException maps to 404 with correct body")
    void customerNotFoundMapsTo404() throws Exception {
        when(rewardsService.calculateRewardsForLastNMonths("X99", 3))
                .thenThrow(new CustomerNotFoundException("X99"));

        mockMvc.perform(get("/api/v1/rewards/X99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status",  is(404)))
                .andExpect(jsonPath("$.error",   is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("X99")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("InvalidDateRangeException maps to 400 with correct body")
    void invalidDateRangeMapsTo400() throws Exception {
        when(rewardsService.calculateRewardsForLastNMonths("C001", 0))
                .thenThrow(new InvalidDateRangeException("months must be between 1 and 36"));

        mockMvc.perform(get("/api/v1/rewards/C001")
                        .param("months", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error",  is("Bad Request")));
    }

    @Test
    @DisplayName("Unexpected RuntimeException maps to 500")
    void unexpectedExceptionMapsTo500() throws Exception {
        when(rewardsService.calculateRewardsForLastNMonths("C001", 3))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/v1/rewards/C001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.message", containsString("unexpected error")));
    }

    @Test
    @DisplayName("Missing required param maps to 400")
    void missingParamMapsTo400() throws Exception {
        // /range endpoint requires startDate & endDate
        mockMvc.perform(get("/api/v1/rewards/C001/range")
                        .param("endDate", "2024-12-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("startDate")));
    }
}
