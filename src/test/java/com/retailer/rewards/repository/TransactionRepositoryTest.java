package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the in-memory {@link TransactionRepository}.
 */
@DisplayName("TransactionRepository Tests")
class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    @DisplayName("findCustomerById returns customer for known ID")
    void findKnownCustomer() {
        Optional<Customer> customer = repository.findCustomerById("C001");
        assertThat(customer).isPresent();
        assertThat(customer.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findCustomerById returns empty for unknown ID")
    void findUnknownCustomer() {
        assertThat(repository.findCustomerById("UNKNOWN")).isEmpty();
    }

    @Test
    @DisplayName("findAllCustomers returns all 3 seeded customers")
    void findAllCustomers() {
        List<Customer> customers = repository.findAllCustomers();
        assertThat(customers).hasSize(3)
                .extracting(Customer::getCustomerId)
                .containsExactlyInAnyOrder("C001", "C002", "C003");
    }

    @Test
    @DisplayName("findTransactionsByCustomerIdAndDateRange returns only matching transactions")
    void findTransactionsInRange() {
        LocalDate start = LocalDate.now().minusMonths(3);
        LocalDate end   = LocalDate.now();

        List<Transaction> txns =
                repository.findTransactionsByCustomerIdAndDateRange("C001", start, end);

        assertThat(txns).isNotEmpty();
        txns.forEach(t -> {
            assertThat(t.getCustomerId()).isEqualTo("C001");
            assertThat(t.getTransactionDate()).isBetween(start, end);
        });
    }

    @Test
    @DisplayName("findTransactionsByCustomerIdAndDateRange returns empty for out-of-range dates")
    void findTransactionsOutOfRange() {
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end   = LocalDate.of(2000, 12, 31);

        List<Transaction> txns =
                repository.findTransactionsByCustomerIdAndDateRange("C001", start, end);

        assertThat(txns).isEmpty();
    }

    @Test
    @DisplayName("findAllTransactions returns an unmodifiable list")
    void findAllTransactionsIsUnmodifiable() {
        List<Transaction> all = repository.findAllTransactions();
        assertThat(all).isNotEmpty();
        assertThatThrownBy(() -> all.add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
