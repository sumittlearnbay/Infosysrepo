package com.retailer.rewards.service;

import com.retailer.rewards.dto.RewardsResponse;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RewardsServiceResilienceTest {

    @Autowired
    private RewardsService rewardsService;

    @MockBean
    private TransactionRepository transactionRepository;

    @Test
    void fallbackReturnsEmptyRewardsWhenRepositoryFails() {
        when(transactionRepository.findCustomerById("C001"))
                .thenThrow(new RuntimeException("database unavailable"));

        RewardsResponse response = rewardsService.calculateRewardsForLastNMonths("C001", 3);

        assertEquals("C001", response.getCustomerId());
        assertEquals("Rewards unavailable", response.getCustomerName());
        assertEquals(0, response.getTotalTransactions());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getTotalRewardPoints()));
        assertTrue(response.getMonthlyBreakdown().isEmpty());
    }

    @Test
    void allCustomersFallbackReturnsEmptyListWhenRepositoryFails() {
        when(transactionRepository.findAllCustomers())
                .thenThrow(new RuntimeException("database unavailable"));

        List<RewardsResponse> response = rewardsService.calculateRewardsForAllCustomers(3);

        assertTrue(response.isEmpty());
    }

    @Test
    void fallbackDoesNotMaskCustomerNotFound() {
        when(transactionRepository.findCustomerById("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.calculateRewardsByDateRange(
                        "UNKNOWN", LocalDate.now().minusDays(1), LocalDate.now()));
    }
}
