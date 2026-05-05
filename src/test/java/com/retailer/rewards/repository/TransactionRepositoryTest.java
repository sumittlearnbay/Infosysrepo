package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    // =========================
    // ✅ CUSTOMER TESTS
    // =========================

    @Test
    void testFindCustomerByIdSuccess() {
        Optional<Customer> customer = repository.findCustomerById("C001");

        assertTrue(customer.isPresent());
        assertEquals("Alice Johnson", customer.get().getName());
    }

    @Test
    void testFindCustomerByIdInvalid() {
        Optional<Customer> customer = repository.findCustomerById("INVALID");

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

        assertEquals(3, customers.size());
    }

    // =========================
    // ✅ TRANSACTION TESTS
    // =========================

    @Test
    void testFindTransactionsByCustomerId() {
        List<Transaction> transactions = repository.findTransactionsByCustomerId("C001");

        assertEquals(3, transactions.size());

        // Verify descending order
        assertTrue(transactions.get(0).getTransactionDate()
                .isAfter(transactions.get(1).getTransactionDate()));
    }

    @Test
    void testFindTransactionsByCustomerIdInvalid() {
        List<Transaction> transactions = repository.findTransactionsByCustomerId("INVALID");

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
                repository.findTransactionsByCustomerIdAndDateRange("C001", start, end);

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
                repository.findTransactionsByCustomerIdAndDateRange("INVALID",
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindTransactionsByDateRangeNullDates() {
        List<Transaction> transactions =
                repository.findTransactionsByCustomerIdAndDateRange("C001", null, null);

        assertTrue(transactions.isEmpty());
    }

    @Test
    void testFindAllTransactions() {
        List<Transaction> transactions = repository.findAllTransactions();

        assertEquals(7, transactions.size());

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
                .customerId("C004")
                .name("Test User")
                .build();

        boolean result = repository.addCustomer(newCustomer);

        assertTrue(result);
        assertEquals(4, repository.getCustomerCount());
    }

    @Test
    void testAddCustomerDuplicate() {
        Customer duplicate = Customer.builder()
                .customerId("C001")
                .name("Duplicate")
                .build();

        boolean result = repository.addCustomer(duplicate);

        assertFalse(result);
    }

    @Test
    void testAddTransactionSuccess() {
        Transaction transaction = Transaction.builder()
                .transactionId("T100")
                .customerId("C001")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("100.00"))
                .build();

        boolean result = repository.addTransaction(transaction);

        assertTrue(result);
        assertEquals(8, repository.getTransactionCount());
    }

    @Test
    void testAddTransactionInvalidCustomer() {
        Transaction transaction = Transaction.builder()
                .transactionId("T101")
                .customerId("INVALID")
                .transactionDate(LocalDate.now())
                .amount(new BigDecimal("100.00"))
                .build();

        boolean result = repository.addTransaction(transaction);

        assertFalse(result);
    }

    @Test
    void testUpdateCustomerSuccess() {
        Customer updated = Customer.builder()
                .customerId("C001")
                .name("Updated Name")
                .build();

        boolean result = repository.updateCustomer(updated);

        assertTrue(result);

        Optional<Customer> customer = repository.findCustomerById("C001");
        assertEquals("Updated Name", customer.get().getName());
    }

    @Test
    void testUpdateCustomerNotFound() {
        Customer updated = Customer.builder()
                .customerId("INVALID")
                .name("Test")
                .build();

        boolean result = repository.updateCustomer(updated);

        assertFalse(result);
    }

    @Test
    void testDeleteCustomerSuccess() {
        boolean result = repository.deleteCustomer("C001");

        assertTrue(result);
        assertEquals(2, repository.getCustomerCount());
    }

    @Test
    void testDeleteCustomerInvalid() {
        boolean result = repository.deleteCustomer("INVALID");

        assertFalse(result);
    }

    @Test
    void testDeleteTransactionSuccess() {
        boolean result = repository.deleteTransaction("T001");

        assertTrue(result);
        assertEquals(6, repository.getTransactionCount());
    }

    @Test
    void testDeleteTransactionInvalid() {
        boolean result = repository.deleteTransaction("INVALID");

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
    void testReinitialize() {
        repository.clear();
        repository.reinitialize();

        assertEquals(3, repository.getCustomerCount());
        assertEquals(7, repository.getTransactionCount());
    }
}