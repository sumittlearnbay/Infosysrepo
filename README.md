Rewards API v2

Customer Rewards Program REST API with parameter-driven routing, BigDecimal precision, and comprehensive validation.



Recent Updates & Enhancements

Based on recent code reviews, the following major improvements have been implemented:

1. Unified Transaction Endpoint
Changed @PostMapping to @GetMapping: The API now correctly uses GET for read-only queries.
Removed queryType parameter: The API now intelligently routes requests based on the combination of provided parameters:
  startDate + endDate → Date range query
  customerId (no dates) → Single customer (Last N months)
  Neither customerId nor dates → All customers (Last N months)

2. Precision & Rounding Logic
BigDecimal Precision: Replaced long with BigDecimal to preserve exact fractional values during processing.
FLOOR Rounding: Added amount.setScale(0, RoundingMode.FLOOR) before point calculations to strictly enforce the "per whole dollar" requirement, ensuring amounts like $50.50 do not incorrectly earn points.
Encapsulation: Made calculatePoints() private to prevent internal implementation details from being exposed.

3. Controller-Level Validation Checkpoints
Implemented strict validation before hitting the service layer:
Conflicting parameters (months + startDate/endDate) → 400 Bad Request
Partial date range (missing start or end) → 400 Bad Request
Missing customerId for date range → 400 Bad Request
Invalid months value (outside 1-36) → 400 Bad Request
endDate before startDate → 400 Bad Request
Future startDate → 400 Bad Request
Invalid date formats automatically handled by Spring (@DateTimeFormat)

4. Exception Handling Improvements
Standardized Error Responses: Created a common response builder in the GlobalExceptionHandler to ensure a consistent structure (status, message, timestamp).
Meaningful Messages: Updated CustomerNotFoundException to output: "Customer not found for ID: {id}".
Improved null safety for type mismatch exceptions.

5. Comprehensive Test Coverage
Repository Tests: Updated findKnownCustomer() to use the correct getName() method. Removed obsolete tests. Added validation for sorting logic (ascending/descending) and seeded data correctness.
Controller Tests: Migrated away from generic any() matchers to exact parameter validation using eq(). Full coverage for all routing paths and failure scenarios.
Service Tests: Full coverage for calculateRewardsByDateRange and calculateRewardsForAllCustomers.



Quick Start

Prerequisites
Java 17+
Maven 3.6+

Build & Run

bash
Build the project
mvn clean verify

Run locally
mvn spring-boot:run


Server starts at: http://localhost:8080



API Endpoints

Single Endpoint: GET /api/v1/rewards

Route 1: Single Customer (Last N Months)
When: customerId provided, no dates

http
GET /api/v1/rewards?customerId=C001&months=3


Route 2: Date Range Query
When: startDate AND endDate provided

http
GET /api/v1/rewards?customerId=C001&startDate=2024-10-01&endDate=2024-12-31


Route 3: All Customers (Last N Months)
When: Neither customerId nor dates provided

http
GET /api/v1/rewards?months=3
GET /api/v1/rewards  (uses default 3 months)




Point Calculation Rules

Uses FLOOR rounding to ignore cents:

| Amount | Calculation | Points |
|--------|-------------|--------|
| $0 - $49.99 | (FLOOR to whole dollar) | 0 |
| $50.00 - $99.99 | (amount - 50) × 1 | 0-49 |
| $100.00+ | (amount - 100) × 2 + 50 | 50+ |

Examples:
$50.50 → FLOOR to $50 → 0 points
$75.00 → ($75 - 50) × 1 = 25 points
$120.75 → FLOOR to $120 → (20 × 2) + 50 = 90 points
$200.00 → (100 × 2) + 50 = 250 points



Testing
bash
mvn clean test


Test coverage includes:
RewardsControllerTest (Routing, Input Validation, HTTP Status Codes)
RewardsServiceTest (Business Logic, Point Calculation, Rounding)
TransactionRepositoryTest (Data Access, Sorting, Filtering)
GlobalExceptionHandlerTest (Standardized Error Formatting)



Configuration

H2 In-Memory Database
H2 console: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:rewardsdb
Username: sa
Password: empty

Seed customers and transactions are inserted at startup from src/main/resources/data.sql.