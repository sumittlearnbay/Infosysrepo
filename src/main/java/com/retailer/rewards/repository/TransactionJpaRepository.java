package com.retailer.rewards.repository;

import com.retailer.rewards.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCustomerIdOrderByTransactionDateDesc(String customerId);

    List<Transaction> findByCustomerIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            String customerId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findAllByOrderByTransactionDateDesc();
}
