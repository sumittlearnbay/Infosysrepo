package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.retailer.rewards.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    // =========================
    // ✅ CUSTOMER TESTS
    // =========================

    @Test
    void testFindCustomerByIdSuccess() {
        Optional<Customer> customer = repository.findCustomerById(CUSTOMER_ID);

        assertTrue(customer.isPresent());
        assertEquals(CUSTOMER_NAME, customer.get().getName());
    }

    @Test
    void testFindCustomerByIdInvalid() {
        Optional<Customer> customer = repository.findCustomerById(INVALID_ID);

        assertFalse(customer.isPresent());
    }

    @Test
    void testFindCustomerByIdNull() {
        Optional<Customer> customer = repository.findCustomerById(null);

        assertFalse(customer.isPresent());
    }

    @Test
    void testFindAllCustomers() {
        List<Customer> customers = repository.findAllCustomers();

        assertEquals(SEEDED_CUSTOMER_COUNT, customers.size());
    }

    // =========================
    // ✅ TRANSACTION TESTS
    // =========================

    @Test
    void testFindTransactionsByCustomerId() {
        List<Transaction> transactions = repository.findTransactionsByCustomerId(CUSTOMER_ID);

        assertEquals(SEEDED_CUSTOMER_COUNT, transactions.size());

        // Verify descending order
        assertTrue(transactions.get(0).getTransactionDate()
                .isAfter(transactions.get(1).getTransactionDate()));
    }

    @Test
    void testFindTransactionsByCustomerIdInvalid() {
        List<Transaction> transactions = repository.findTransactionsByCustomerId(INVALID_ID);

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindTransactionsByCustomerIdNull() {
        List<Transaction> transactions = repository.findTransactionsByCustomerId(null);

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindTransactionsByCustomerIdAndDateRange() {
        LocalDate start = LocalDate.now().minusMonths(3);
        LocalDate end = LocalDate.now();

        List<Transaction> transactions =
                repository.findTransactionsByCustomerIdAndDateRange(CUSTOMER_ID, start, end);

        assertFalse(transactions.isEmpty());

        // Verify ascending order
        for (int i = 0; i < transactions.size() - 1; i++) {
            assertFalse(transactions.get(i).getTransactionDate()
                    .isAfter(transactions.get(i + 1).getTransactionDate()));
        }
    }

    @Test
    void testFindTransactionsByDateRangeInvalidCustomer() {
        List<Transaction> transactions =
                repository.findTransactionsByCustomerIdAndDateRange(INVALID_ID,
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindTransactionsByDateRangeNullDates() {
        List<Transaction> transactions =
                repository.findTransactionsByCustomerIdAndDateRange(CUSTOMER_ID, null, null);

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindAllTransactions() {
        List<Transaction> transactions = repository.findAllTransactions();

        assertEquals(SEEDED_TRANSACTION_COUNT, transactions.size());

        // Verify descending order
        assertTrue(transactions.get(0).getTransactionDate()
                .isAfter(transactions.get(1).getTransactionDate()));
    }

    // =========================
    // ✅ ADD / UPDATE / DELETE
    // =========================

    @Test
    void testAddCustomerSuccess() {
        Customer newCustomer = Customer.builder()
                .customerId(NEW_CUSTOMER_ID)
                .name(TEST_CUSTOMER_NAME)
                .build();

        boolean result = repository.addCustomer(newCustomer);

        assertTrue(result);
        assertEquals(CUSTOMER_COUNT_AFTER_ADD, repository.getCustomerCount());
    }

    @Test
    void testAddCustomerDuplicate() {
        Customer duplicate = Customer.builder()
                .customerId(CUSTOMER_ID)
                .name(DUPLICATE_CUSTOMER_NAME)
                .build();

        boolean result = repository.addCustomer(duplicate);

        assertFalse(result);
    }

    @Test
    void testAddTransactionSuccess() {
        Transaction transaction = Transaction.builder()
                .transactionId(NEW_TRANSACTION_ID)
                .customerId(CUSTOMER_ID)
                .transactionDate(LocalDate.now())
                .amount(TRANSACTION_AMOUNT)
                .build();

        boolean result = repository.addTransaction(transaction);

        assertTrue(result);
        assertEquals(TRANSACTION_COUNT_AFTER_ADD, repository.getTransactionCount());
    }

    @Test
    void testAddTransactionInvalidCustomer() {
        Transaction transaction = Transaction.builder()
                .transactionId(INVALID_TRANSACTION_ID)
                .customerId(INVALID_ID)
                .transactionDate(LocalDate.now())
                .amount(TRANSACTION_AMOUNT)
                .build();

        boolean result = repository.addTransaction(transaction);

        assertFalse(result);
    }

    @Test
    void testUpdateCustomerSuccess() {
        Customer updated = Customer.builder()
                .customerId(CUSTOMER_ID)
                .name(UPDATED_CUSTOMER_NAME)
                .build();

        boolean result = repository.updateCustomer(updated);

        assertTrue(result);

        Optional<Customer> customer = repository.findCustomerById(CUSTOMER_ID);
        assertEquals(UPDATED_CUSTOMER_NAME, customer.get().getName());
    }

    @Test
    void testUpdateCustomerNotFound() {
        Customer updated = Customer.builder()
                .customerId(INVALID_ID)
                .name(TEST_CUSTOMER_NAME)
                .build();

        boolean result = repository.updateCustomer(updated);

        assertFalse(result);
    }

    @Test
    void testDeleteCustomerSuccess() {
        boolean result = repository.deleteCustomer(CUSTOMER_ID);

        assertTrue(result);
        assertEquals(CUSTOMER_COUNT_AFTER_DELETE, repository.getCustomerCount());
    }

    @Test
    void testDeleteCustomerInvalid() {
        boolean result = repository.deleteCustomer(INVALID_ID);

        assertFalse(result);
    }

    @Test
    void testDeleteTransactionSuccess() {
        boolean result = repository.deleteTransaction(TRANSACTION_ID);

        assertTrue(result);
        assertEquals(TRANSACTION_COUNT_AFTER_DELETE, repository.getTransactionCount());
    }

    @Test
    void testDeleteTransactionInvalid() {
        boolean result = repository.deleteTransaction(INVALID_ID);

        assertFalse(result);
    }

    // =========================
    // ✅ UTILITY TESTS
    // =========================

    @Test
    void testClear() {
        repository.clear();

        assertEquals(0, repository.getCustomerCount());
        assertEquals(0, repository.getTransactionCount());
    }

    @Test
    void testCountsSeededDataFromSql() {
        assertEquals(SEEDED_CUSTOMER_COUNT, repository.getCustomerCount());
        assertEquals(SEEDED_TRANSACTION_COUNT, repository.getTransactionCount());
    }
}
