package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for managing customers and transactions.
 * Provides CRUD operations and custom query methods for rewards calculation.
 *
 * This implementation uses seeded test data for development and testing purposes.
 */
@Repository
public class TransactionRepository {

    private final Map<String, Customer> customers;
    private final List<Transaction> transactions;

    /**
     * Constructor initializes the repository and seeds it with sample data.
     */
    public TransactionRepository() {
        this.customers = new HashMap<>();
        this.transactions = new ArrayList<>();
        seedData();
    }

    /**
     * Seeds the repository with sample customers and transactions.
     * This data is used for development and testing purposes.
     */
    private void seedData() {
        seedCustomers();
        seedTransactions();
    }

    /**
     * Seeds the repository with 3 sample customers across different membership tiers.
     */
    private void seedCustomers() {
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
    }

    /**
     * Seeds the repository with 7 sample transactions across 3 customers
     * covering the last 3 months.
     */
    private void seedTransactions() {
        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusMonths(3);

        // C001 transactions (3 transactions)
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

        // C002 transactions (2 transactions)
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

        // C003 transactions (2 transactions)
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

    // =====================================================================
    // Customer Query Methods
    // =====================================================================

    /**
     * Finds a customer by their customer ID.
     *
     * @param customerId the customer ID to search for
     * @return Optional containing the customer if found, empty Optional otherwise
     */
    public Optional<Customer> findCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(customers.get(customerId));
    }

    /**
     * Retrieves all customers in the repository.
     * Returns a copy of the customer list to prevent external modifications.
     *
     * @return List of all customers (unmodifiable)
     */
    public List<Customer> findAllCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers.values()));
    }

    // =====================================================================
    // Transaction Query Methods
    // =====================================================================

    /**
     * Finds all transactions for a specific customer within a date range.
     * Results are sorted by transaction date in ascending order.
     *
     * @param customerId the customer ID to filter by
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of transactions sorted by date, or empty list if none found
     */
    public List<Transaction> findTransactionsByCustomerIdAndDateRange(
            String customerId, LocalDate startDate, LocalDate endDate) {

        // Validate inputs
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .filter(t -> t.getCustomerId().equals(customerId))
                .filter(t -> !t.getTransactionDate().isBefore(startDate))
                .filter(t -> !t.getTransactionDate().isAfter(endDate))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    /**
     * Finds all transactions for a specific customer.
     * Results are sorted by transaction date in descending order (most recent first).
     *
     * @param customerId the customer ID to filter by
     * @return List of transactions sorted by date descending, or empty list if none found
     */
    public List<Transaction> findTransactionsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return transactions.stream()
                .filter(t -> t.getCustomerId().equals(customerId))
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all transactions in the repository.
     * Results are sorted by transaction date in descending order (most recent first).
     *
     * @return List of all transactions sorted by date descending
     */
    public List<Transaction> findAllTransactions() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    // =====================================================================
    // Data Modification Methods (Optional - for future enhancements)
    // =====================================================================

    /**
     * Adds a new customer to the repository.
     *
     * @param customer the customer to add
     * @return true if customer was added, false if customer ID already exists
     */
    public boolean addCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return false;
        }
        return customers.putIfAbsent(customer.getCustomerId(), customer) == null;
    }

    /**
     * Adds a new transaction to the repository.
     *
     * @param transaction the transaction to add
     * @return true if transaction was added successfully
     */
    public boolean addTransaction(Transaction transaction) {
        if (transaction == null || transaction.getTransactionId() == null) {
            return false;
        }
        // Check if customer exists
        if (!customers.containsKey(transaction.getCustomerId())) {
            return false;
        }
        return transactions.add(transaction);
    }

    /**
     * Updates an existing customer in the repository.
     *
     * @param customer the customer to update
     * @return true if customer was updated, false if customer ID not found
     */
    public boolean updateCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return false;
        }
        if (!customers.containsKey(customer.getCustomerId())) {
            return false;
        }
        customers.put(customer.getCustomerId(), customer);
        return true;
    }

    /**
     * Removes a customer from the repository.
     *
     * @param customerId the customer ID to remove
     * @return true if customer was removed, false if customer ID not found
     */
    public boolean deleteCustomer(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return false;
        }
        return customers.remove(customerId) != null;
    }

    /**
     * Removes a transaction from the repository.
     *
     * @param transactionId the transaction ID to remove
     * @return true if transaction was removed, false if transaction ID not found
     */
    public boolean deleteTransaction(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return false;
        }
        return transactions.removeIf(t -> t.getTransactionId().equals(transactionId));
    }

    // =====================================================================
    // Utility Methods
    // =====================================================================

    /**
     * Returns the total number of customers in the repository.
     *
     * @return number of customers
     */
    public int getCustomerCount() {
        return customers.size();
    }

    /**
     * Returns the total number of transactions in the repository.
     *
     * @return number of transactions
     */
    public int getTransactionCount() {
        return transactions.size();
    }

    /**
     * Clears all data from the repository.
     * Useful for testing purposes.
     */
    public void clear() {
        customers.clear();
        transactions.clear();
    }

    /**
     * Reinitializes the repository with fresh seeded data.
     * Useful for test reset scenarios.
     */
    public void reinitialize() {
        clear();
        seedData();
    }
}