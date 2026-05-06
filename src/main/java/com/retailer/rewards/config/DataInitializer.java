package com.retailer.rewards.config;

import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedRewardsData(TransactionRepository transactionRepository) {
        return args -> transactionRepository.seedDataIfEmpty();
    }
}
