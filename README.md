# 🏆 Retailer Rewards API

A Spring Boot RESTful API that calculates customer reward points based on purchase transactions.

---

## 📐 Design Details

### Rewards Calculation Rules

| Spend Tier | Points Earned |
|------------|--------------|
| $0 – $50   | 0 points     |
| $50 – $100 | 1 point per dollar (e.g. $75 → 25 pts) |
| Over $100  | 2 points per dollar above $100, **plus** 1 point per dollar between $50–$100 |

**Example:**
> A **$120** purchase = `2 × $20` + `1 × $50` = **90 points**

### Architecture

```
┌─────────────────────────────────────────────┐
│              REST Controller                │
│         RewardsController                   │
│  POST /api/v1/transactions                  │
│    (queryType: single, range, all)          │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│            Service Layer                    │
│          RewardsService                     │
│  • Point calculation logic (BigDecimal)     │
│  • Monthly grouping                         │
│  • Date range validation                    │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         Repository (In-Memory)              │
│       TransactionRepository                 │
│  • 3 sample customers (C001/C002/C003)      │
│  • 21 seeded transactions (last 3 months)   │
└─────────────────────────────────────────────┘
```

### Package Structure

```
src/
├── main/java/com/retailer/rewards/
│   ├── RewardsApplication.java          # Spring Boot entry point
│   ├── controller/
│   │   └── RewardsController.java       # REST endpoints
│   ├── service/
│   │   └── RewardsService.java          # Business logic + point calculation
│   ├── repository/
│   │   └── TransactionRepository.java   # In-memory data store
│   ├── model/
│   │   ├── Customer.java
│   │   └── Transaction.java
│   ├── dto/
│   │   ├── RewardsResponse.java         # Top-level API response
│   │   ├── MonthlyRewardsSummary.java   # Per-month breakdown
│   │   ├── TransactionRewardDetail.java # Per-transaction detail
│   │   └── ApiErrorResponse.java        # Error envelope
│   └── exception/
│       ├── CustomerNotFoundException.java
│       ├── InvalidDateRangeException.java
│       └── GlobalExceptionHandler.java  # Centralised error mapping
└── test/java/com/retailer/rewards/
    ├── service/
    │   └── RewardsServiceTest.java      # Unit tests (Mockito)
    ├── controller/
    │   └── RewardsControllerTest.java   # MockMvc tests
    ├── repository/
    │   └── TransactionRepositoryTest.java
    └── exception/
        └── GlobalExceptionHandlerTest.java
```

---

## 🔧 Technical Details

| Component | Technology |
|-----------|-----------|
| Language  | Java 8    |
| Framework | Spring Boot 2.7.18 |
| Build     | Maven 3.x |
| Testing   | JUnit 5, Mockito, MockMvc |
| Coverage  | JaCoCo (≥ 80% line coverage enforced) |
| Logging   | SLF4J + Logback |
| JSON      | Jackson (dates as ISO strings) |

### Prerequisites

- Java 8+
- Maven 3.6+

---

## 🚀 Getting Started

### Build & Run

```bash
# Clone / navigate to project root
cd rewards-api

# Build (skip tests)
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Application starts on http://localhost:8080
```

### Run Tests + Coverage Report

```bash
# Run all tests and generate JaCoCo HTML report
mvn clean verify

# View coverage report
open target/site/jacoco/index.html
```

Coverage threshold: **80% line coverage** (enforced by JaCoCo; build fails below this).

---

## 📡 API Details

### Base URL

```
http://localhost:8080/api/v1
```

---

### `POST /api/v1/transactions`

Unified endpoint for handling all transaction reward queries. Route behavior based on `queryType` parameter.

| Parameter    | Type    | Location | Required | Description                               |
|-------------|---------|----------|----------|-------------------------------------------|
| `queryType`  | String  | Query    | ✅       | Query mode: "single", "range", or "all"   |
| `customerId` | String  | Query    | ⚠️       | Customer ID (required for single/range)   |
| `months`     | Integer | Query    | ❌       | Look-back window for single/all (1–36)    |
| `startDate`  | LocalDate | Query  | ⚠️       | Range start (required for range, YYYY-MM-DD) |
| `endDate`    | LocalDate | Query  | ⚠️       | Range end (required for range, YYYY-MM-DD)  |

#### Mode: `queryType=single`

Calculate rewards for a single customer over the last N months.

**Sample Request**
```http
POST /api/v1/transactions?queryType=single&customerId=C001&months=3
```

**Sample Response (200 OK)**
```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "membershipTier": "GOLD",
  "periodStart": "2024-10-01",
  "periodEnd": "2024-12-31",
  "monthsCovered": 3,
  "totalTransactions": 9,
  "totalRewardPoints": 575,
  "monthlyBreakdown": [
    {
      "year": 2024,
      "month": 10,
      "monthName": "OCTOBER",
      "monthlyPoints": 230,
      "transactions": [
        {
          "transactionId": "T007",
          "transactionDate": "2024-10-08",
          "amount": 150.00,
          "pointsEarned": 150,
          "description": "Apple Store"
        }
      ]
    }
  ]
}
```

---

#### Mode: `queryType=range`

Calculate rewards for a single customer within an explicit date range.

**Sample Request**
```http
POST /api/v1/transactions?queryType=range&customerId=C001&startDate=2024-10-01&endDate=2024-12-31
```

---

#### Mode: `queryType=all`

Calculate rewards for **all** customers over the last N months.

**Sample Request**
```http
POST /api/v1/transactions?queryType=all&months=3
```

Returns a JSON array with one `RewardsResponse` per customer.

---

### Error Responses

All errors follow a consistent envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with ID: C999",
  "path": "/api/v1/rewards/C999",
  "timestamp": "2024-12-01T10:30:00",
  "details": null
}
```

| HTTP Status | Cause |
|-------------|-------|
| `400`       | Invalid `months` value, bad date format, end before start, future start date |
| `404`       | Unknown `customerId` |
| `500`       | Unexpected server error |

---

## 🧪 Sample Data

The in-memory store ships with three customers and 21 transactions spanning the last 3 calendar months.

| Customer ID | Name            | Tier     |
|------------|-----------------|----------|
| `C001`      | Alice Johnson   | GOLD     |
| `C002`      | Bob Smith       | SILVER   |
| `C003`      | Carol Williams  | PLATINUM |

---

## 📬 Postman Collection

Import `rewards-api-postman-collection.json` (located in project root) into Postman.

The collection covers:
- ✅ All 3 endpoints with happy-path scenarios for each customer
- ✅ Custom `months` parameters (1, 3, 6)
- ✅ Date-range queries with dynamic pre-request scripts
- ✅ All error scenarios (404, 400 – missing param, bad type, out-of-range, invalid dates)
- ✅ Built-in Postman test scripts asserting status codes, response shapes, and field values

### Quick Start

1. Open Postman → **Import** → select `rewards-api-postman-collection.json`
2. Start the Spring Boot application (`mvn spring-boot:run`)
3. Run the entire collection via **Collection Runner**

---

## 🌐 Health Check

```http
GET /actuator/health   → { "status": "UP" }
GET /actuator/info     → app name, version, description
```

---

## 📋 Code Quality Notes

- **Naming conventions**: camelCase variables/methods, PascalCase classes, UPPER_SNAKE_CASE constants
- **Logging**: SLF4J at INFO for requests, DEBUG for internal processing, WARN for validation failures, ERROR for unexpected exceptions
- **Exception handling**: Centralised via `@RestControllerAdvice` — no raw stack traces exposed
- **Input validation**: `months` range check (1–36), date range logic, null guards on amounts
- **No console `System.out` calls** — all output via SLF4J logger
- **Immutable response DTOs** built with Lombok `@Builder`
