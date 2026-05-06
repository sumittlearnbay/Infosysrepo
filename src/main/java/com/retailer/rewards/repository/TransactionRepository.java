package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository facade for customer and transaction queries.
 *
 * The public methods preserve the original API used by the service layer while
 * storing data in H2 through Spring Data JPA.
 */
@Repository
public class TransactionRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    public TransactionRepository(CustomerJpaRepository customerJpaRepository,
                                 TransactionJpaRepository transactionJpaRepository) {
        this.customerJpaRepository = customerJpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
    }

    public Optional<Customer> findCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        return customerJpaRepository.findById(customerId);
    }

    public List<Customer> findAllCustomers() {
        return customerJpaRepository.findAll();
    }

    public List<Transaction> findTransactionsByCustomerIdAndDateRange(
            String customerId, LocalDate startDate, LocalDate endDate) {

        if (customerId == null || customerId.trim().isEmpty() || startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        return transactionJpaRepository.findByCustomerIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                customerId, startDate, endDate);
    }

    public List<Transaction> findTransactionsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return transactionJpaRepository.findByCustomerIdOrderByTransactionDateDesc(customerId);
    }

    public List<Transaction> findAllTransactions() {
        return transactionJpaRepository.findAllByOrderByTransactionDateDesc();
    }

    @Transactional
    public boolean addCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return false;
        }
        if (customerJpaRepository.existsById(customer.getCustomerId())) {
            return false;
        }
        customerJpaRepository.save(customer);
        return true;
    }

    @Transactional
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null || transaction.getTransactionId() == null) {
            return false;
        }
        if (transaction.getCustomerId() == null || !customerJpaRepository.existsById(transaction.getCustomerId())) {
            return false;
        }
        transactionJpaRepository.save(transaction);
        return true;
    }

    @Transactional
    public boolean updateCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return false;
        }
        if (!customerJpaRepository.existsById(customer.getCustomerId())) {
            return false;
        }
        customerJpaRepository.save(customer);
        return true;
    }

    @Transactional
    public boolean deleteCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty() || !customerJpaRepository.existsById(customerId)) {
            return false;
        }
        customerJpaRepository.deleteById(customerId);
        return true;
    }

    @Transactional
    public boolean deleteTransaction(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()
                || !transactionJpaRepository.existsById(transactionId)) {
            return false;
        }
        transactionJpaRepository.deleteById(transactionId);
        return true;
    }

    public int getCustomerCount() {
        return (int) customerJpaRepository.count();
    }

    public int getTransactionCount() {
        return (int) transactionJpaRepository.count();
    }

    @Transactional
    public void clear() {
        transactionJpaRepository.deleteAll();
        customerJpaRepository.deleteAll();
    }

    @Transactional
    public void reinitialize() {
        clear();
        seedData();
    }

    @Transactional
    public void seedDataIfEmpty() {
        if (customerJpaRepository.count() == 0 && transactionJpaRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        seedCustomers();
        seedTransactions();
    }

    private void seedCustomers() {
        customerJpaRepository.save(Customer.builder()
                .customerId("C001")
                .name("Alice Johnson")
                .email("alice@example.com")
                .membershipTier("GOLD")
                .build());

        customerJpaRepository.save(Customer.builder()
                .customerId("C002")
                .name("Bob Smith")
                .email("bob@example.com")
                .membershipTier("SILVER")
                .build());

        customerJpaRepository.save(Customer.builder()
                .customerId("C003")
                .name("Carol White")
                .email("carol@example.com")
                .membershipTier("BRONZE")
                .build());
    }

    private void seedTransactions() {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusMonths(3);

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T001")
                .customerId("C001")
                .transactionDate(threeMonthsAgo.plusDays(5))
                .amount(new BigDecimal("120.75"))
                .description("Electronics Purchase")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T002")
                .customerId("C001")
                .transactionDate(threeMonthsAgo.plusDays(15))
                .amount(new BigDecimal("75.50"))
                .description("Grocery Store")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T003")
                .customerId("C001")
                .transactionDate(today.minusDays(10))
                .amount(new BigDecimal("200.00"))
                .description("Fashion Retail")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T004")
                .customerId("C002")
                .transactionDate(threeMonthsAgo.plusDays(8))
                .amount(new BigDecimal("95.25"))
                .description("Restaurant")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T005")
                .customerId("C002")
                .transactionDate(today.minusDays(5))
                .amount(new BigDecimal("150.00"))
                .description("Home Goods")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T006")
                .customerId("C003")
                .transactionDate(threeMonthsAgo.plusDays(2))
                .amount(new BigDecimal("45.99"))
                .description("Books")
                .build());

        transactionJpaRepository.save(Transaction.builder()
                .transactionId("T007")
                .customerId("C003")
                .transactionDate(today.minusDays(1))
                .amount(new BigDecimal("125.50"))
                .description("Cosmetics")
                .build());
    }
}
