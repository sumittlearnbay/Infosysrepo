package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
}
