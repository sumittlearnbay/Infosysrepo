package com.retailer.rewards.repository;

import com.retailer.rewards.model.Customer;
import com.retailer.rewards.model.Transaction;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * In-memory data store seeded with sample customers and transactions.
 * <p>
 * In a production system this would be backed by MongoDB or another
 * persistent store.  The data here covers the last three calendar months
 * so the default API scenario works out of the box.
 */
@Repository
public class TransactionRepository {

    private static final Map<String, Customer> CUSTOMERS = new LinkedHashMap<>();
    private static final List<Transaction> TRANSACTIONS = new ArrayList<>();

    static {
        // ── Customers ────────────────────────────────────────────────────────
        LocalDate now = LocalDate.now();

        CUSTOMERS.put("C001", Customer.builder()
                .customerId("C001")
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.johnson@example.com")
                .membershipTier("GOLD")
                .build());

        CUSTOMERS.put("C002", Customer.builder()
                .customerId("C002")
                .firstName("Bob")
                .lastName("Smith")
                .email("bob.smith@example.com")
                .membershipTier("SILVER")
                .build());

        CUSTOMERS.put("C003", Customer.builder()
                .customerId("C003")
                .firstName("Carol")
                .lastName("Williams")
                .email("carol.williams@example.com")
                .membershipTier("PLATINUM")
                .build());

        // ── Transactions – last 3 months relative to today ───────────────────

        // Alice – month 0 (current month)
        TRANSACTIONS.add(tx("T001", "C001", "120.00", now.withDayOfMonth(5),  "Amazon"));
        TRANSACTIONS.add(tx("T002", "C001",  "75.00", now.withDayOfMonth(12), "Target"));
        TRANSACTIONS.add(tx("T003", "C001",  "40.00", now.withDayOfMonth(18), "Starbucks"));

        // Alice – month -1
        TRANSACTIONS.add(tx("T004", "C001", "200.00", now.minusMonths(1).withDayOfMonth(3),  "Best Buy"));
        TRANSACTIONS.add(tx("T005", "C001",  "85.00", now.minusMonths(1).withDayOfMonth(14), "Walmart"));
        TRANSACTIONS.add(tx("T006", "C001",  "55.00", now.minusMonths(1).withDayOfMonth(22), "Whole Foods"));

        // Alice – month -2
        TRANSACTIONS.add(tx("T007", "C001", "150.00", now.minusMonths(2).withDayOfMonth(8),  "Apple Store"));
        TRANSACTIONS.add(tx("T008", "C001",  "30.00", now.minusMonths(2).withDayOfMonth(17), "Dunkin"));
        TRANSACTIONS.add(tx("T009", "C001", "110.00", now.minusMonths(2).withDayOfMonth(25), "Nike"));

        // Bob – month 0
        TRANSACTIONS.add(tx("T010", "C002",  "60.00", now.withDayOfMonth(7),  "Home Depot"));
        TRANSACTIONS.add(tx("T011", "C002", "130.00", now.withDayOfMonth(15), "Costco"));

        // Bob – month -1
        TRANSACTIONS.add(tx("T012", "C002",  "45.00", now.minusMonths(1).withDayOfMonth(6),  "CVS"));
        TRANSACTIONS.add(tx("T013", "C002", "180.00", now.minusMonths(1).withDayOfMonth(20), "Lowe's"));

        // Bob – month -2
        TRANSACTIONS.add(tx("T014", "C002",  "95.00", now.minusMonths(2).withDayOfMonth(10), "Macy's"));
        TRANSACTIONS.add(tx("T015", "C002", "250.00", now.minusMonths(2).withDayOfMonth(28), "Samsung Store"));

        // Carol – month 0
        TRANSACTIONS.add(tx("T016", "C003", "500.00", now.withDayOfMonth(2),  "Louis Vuitton"));
        TRANSACTIONS.add(tx("T017", "C003",  "75.00", now.withDayOfMonth(9),  "Sephora"));

        // Carol – month -1
        TRANSACTIONS.add(tx("T018", "C003", "320.00", now.minusMonths(1).withDayOfMonth(1),  "Gucci"));
        TRANSACTIONS.add(tx("T019", "C003",  "49.00", now.minusMonths(1).withDayOfMonth(15), "Barnes & Noble"));

        // Carol – month -2
        TRANSACTIONS.add(tx("T020", "C003", "410.00", now.minusMonths(2).withDayOfMonth(5),  "Nordstrom"));
        TRANSACTIONS.add(tx("T021", "C003", "100.00", now.minusMonths(2).withDayOfMonth(19), "Pottery Barn"));
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public Optional<Customer> findCustomerById(String customerId) {
        return Optional.ofNullable(CUSTOMERS.get(customerId));
    }

    public List<Customer> findAllCustomers() {
        return new ArrayList<>(CUSTOMERS.values());
    }

    public List<Transaction> findTransactionsByCustomerIdAndDateRange(
            String customerId, LocalDate startDate, LocalDate endDate) {

        List<Transaction> result = new ArrayList<>();
        for (Transaction t : TRANSACTIONS) {
            if (t.getCustomerId().equals(customerId)
                    && !t.getTransactionDate().isBefore(startDate)
                    && !t.getTransactionDate().isAfter(endDate)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> findAllTransactions() {
        return Collections.unmodifiableList(TRANSACTIONS);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static Transaction tx(String id, String custId, String amount,
                                   LocalDate date, String desc) {
        return Transaction.builder()
                .transactionId(id)
                .customerId(custId)
                .amount(new BigDecimal(amount))
                .transactionDate(date)
                .description(desc)
                .build();
    }
}
