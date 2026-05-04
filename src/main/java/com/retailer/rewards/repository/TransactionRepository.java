package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository with seeded test data.
 */
@Repository
public class TransactionRepository {

    private final Map<String, Customer> customers;
    private final List<Transaction> transactions;

    public TransactionRepository() {
        this.customers = new HashMap<>();
        this.transactions = new ArrayList<>();
        seedData();
    }

    /**
     * Seeds repository with sample customers and transactions.
     */
    private void seedData() {
        // Create sample customers
        customers.put("C001", Customer.builder()
                .customerId("C001")
                .name("Alice Johnson")
                .email("alice@example.com")
                .membershipTier("GOLD")
                .build());

        customers.put("C002", Customer.builder()
                .customerId("C002")
                .name("Bob Smith")
                .email("bob@example.com")
                .membershipTier("SILVER")
                .build());

        customers.put("C003", Customer.builder()
                .customerId("C003")
                .name("Carol White")
                .email("carol@example.com")
                .membershipTier("BRONZE")
                .build());

        // Seed transactions for last 3 months
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusMonths(3);

        // C001 transactions
        transactions.add(Transaction.builder()
                .transactionId("T001")
                .customerId("C001")
                .transactionDate(threeMonthsAgo.plusDays(5))
                .amount(new BigDecimal("120.75"))
                .description("Electronics Purchase")
                .build());

        transactions.add(Transaction.builder()
                .transactionId("T002")
                .customerId("C001")
                .transactionDate(threeMonthsAgo.plusDays(15))
                .amount(new BigDecimal("75.50"))
                .description("Grocery Store")
                .build());

        transactions.add(Transaction.builder()
                .transactionId("T003")
                .customerId("C001")
                .transactionDate(today.minusDays(10))
                .amount(new BigDecimal("200.00"))
                .description("Fashion Retail")
                .build());

        // C002 transactions
        transactions.add(Transaction.builder()
                .transactionId("T004")
                .customerId("C002")
                .transactionDate(threeMonthsAgo.plusDays(8))
                .amount(new BigDecimal("95.25"))
                .description("Restaurant")
                .build());

        transactions.add(Transaction.builder()
                .transactionId("T005")
                .customerId("C002")
                .transactionDate(today.minusDays(5))
                .amount(new BigDecimal("150.00"))
                .description("Home Goods")
                .build());

        // C003 transactions
        transactions.add(Transaction.builder()
                .transactionId("T006")
                .customerId("C003")
                .transactionDate(threeMonthsAgo.plusDays(2))
                .amount(new BigDecimal("45.99"))
                .description("Books")
                .build());

        transactions.add(Transaction.builder()
                .transactionId("T007")
                .customerId("C003")
                .transactionDate(today.minusDays(1))
                .amount(new BigDecimal("125.50"))
                .description("Cosmetics")
                .build());
    }

    /**
     * Finds a customer by ID.
     */
    public Optional<Customer> findCustomerById(String customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }

    /**
     * Finds transactions for a customer within a date range.
     */
    public List<Transaction> findTransactionsByCustomerIdAndDateRange(
            String customerId, LocalDate startDate, LocalDate endDate) {
        return transactions.stream()
                .filter(t -> t.getCustomerId().equals(customerId))
                .filter(t -> !t.getTransactionDate().isBefore(startDate))
                .filter(t -> !t.getTransactionDate().isAfter(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    /**
     * Returns all customers.
     */
    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customers.values());
    }
}
