# 🎁 Rewards API v2

**Customer Rewards Program REST API** with parameter-driven routing, BigDecimal precision, and comprehensive validation.

---

## 📊 Features

✅ **Parameter-Driven Routing** - Single GET endpoint with intelligent parameter routing
✅ **BigDecimal Precision** - Accurate decimal calculations with FLOOR rounding
✅ **Comprehensive Validation** - All validations at controller level
✅ **Three Query Modes**:
   - Single customer by ID (last N months)
   - Single customer by date range
   - All customers (last N months)

✅ **FLOOR Rounding** - Only whole dollars count (e.g., $50.50 → $50)
✅ **Private calculatePoints()** - Internal implementation detail, not exposed
✅ **Full Test Coverage** - Unit tests for all scenarios

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Build & Run

```bash
# Extract and navigate to project
cd rewards-api-v2

# Build
mvn clean verify

# Run
mvn spring-boot:run
```

**Server starts at:** `http://localhost:8080`

---

## 📡 API Endpoints

### Single Endpoint: `GET /api/v1/rewards`

Routes automatically based on parameter combination.

#### Route 1: Single Customer (Last N Months)
**When:** `customerId` provided, no dates

```http
GET /api/v1/rewards?customerId=C001&months=3
```

**Response (200 OK):**
```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "email": "alice@example.com",
  "membershipTier": "GOLD",
  "periodStart": "2024-02-04",
  "periodEnd": "2024-05-04",
  "monthsCovered": 3,
  "totalTransactions": 5,
  "totalRewardPoints": 365,
  "monthlyBreakdown": [...]
}
```

---

#### Route 2: Date Range Query
**When:** `startDate` AND `endDate` provided

```http
GET /api/v1/rewards?customerId=C001&startDate=2024-10-01&endDate=2024-12-31
```

**Response (200 OK):** Same as Route 1

---

#### Route 3: All Customers (Last N Months)
**When:** Neither `customerId` nor dates provided

```http
GET /api/v1/rewards?months=3
GET /api/v1/rewards  (uses default 3 months)
```

**Response (200 OK):**
```json
[
  {
    "customerId": "C001",
    "customerName": "Alice Johnson",
    ...
  },
  {
    "customerId": "C002",
    "customerName": "Bob Smith",
    ...
  }
]
```

---

## ✅ Validation Rules

All validations performed at **controller level** before service call:

| Validation | Condition | Response |
|-----------|-----------|----------|
| **Conflicting Parameters** | `months` AND (`startDate` OR `endDate`) | 400 Bad Request |
| **Partial Date Range** | Only `startDate` OR only `endDate` | 400 Bad Request |
| **Missing CustomerId** | Date range query without `customerId` | 400 Bad Request |
| **Invalid Months** | `months < 1` OR `months > 36` | 400 Bad Request |
| **End Before Start** | `endDate < startDate` | 400 Bad Request |
| **Future Start Date** | `startDate > today` | 400 Bad Request |
| **Unknown Customer** | `customerId` doesn't exist | 404 Not Found |

---

## 🧮 Point Calculation Rules

Uses **FLOOR rounding** to ignore cents:

| Amount | Calculation | Points |
|--------|-------------|--------|
| $0 - $49.99 | (FLOOR to whole dollar) | 0 |
| $50.00 - $99.99 | `(amount - 50) × 1` | 0-49 |
| $100.00+ | `(amount - 100) × 2 + 50` | 50+ |

**Examples:**
- $50.50 → FLOOR to $50 → 0 points
- $75.00 → ($75 - $50) × 1 = 25 points
- $120.75 → FLOOR to $120 → ($20 × 2) + $50 = **90 points**
- $200.00 → ($100 × 2) + $50 = **250 points**

---

## 🏗️ Project Structure

```
src/main/java/com/retailer/rewards/
├── RewardsApplication.java          # Spring Boot entry point
├── controller/
│   └── RewardsController.java       # Single endpoint with validation
├── service/
│   └── RewardsService.java          # Business logic (private calculatePoints)
├── repository/
│   └── TransactionRepository.java   # In-memory data store
├── dto/
│   ├── RewardsResponse.java
│   ├── MonthlyRewardsSummary.java
│   ├── TransactionRewardDetail.java
│   └── ApiErrorResponse.java
├── model/
│   ├── Customer.java
│   └── Transaction.java
└── exception/
    ├── CustomerNotFoundException.java
    ├── InvalidDateRangeException.java
    └── GlobalExceptionHandler.java
```

---

## 🧪 Testing

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test
```bash
mvn test -Dtest=RewardsControllerTest
mvn test -Dtest=RewardsServiceTest
```

### Test Coverage
```bash
mvn clean verify
mvn jacoco:report
```

### Test Classes
- **RewardsControllerTest** - 15+ test cases covering:
  - Single customer queries
  - Date range queries
  - All customers queries
  - All validation scenarios
  
- **RewardsServiceTest** - 10+ test cases covering:
  - BigDecimal precision
  - FLOOR rounding
  - Point calculations
  - Multiple transactions
  - Exception handling

---

## 📮 Testing with Postman

### Import Collection
1. Open Postman
2. Click **Import**
3. Select `rewards-api-postman-collection.json`

### Sample Requests Included
- ✅ Single customer (default 3 months)
- ✅ Single customer (custom months)
- ✅ Date range query
- ✅ All customers (default)
- ✅ All customers (custom months)
- ❌ Invalid months validation
- ❌ Conflicting parameters
- ❌ Missing date range parameters
- ❌ Future start date
- ❌ End before start
- ❌ Unknown customer

---

## 📊 Seeded Data

Repository comes pre-populated with:

**Customers:**
- C001: Alice Johnson (GOLD)
- C002: Bob Smith (SILVER)
- C003: Carol White (BRONZE)

**Transactions:** 7 transactions across 3 months

---

## 🔐 Key Implementation Details

### 1. BigDecimal Precision
- All point fields use `BigDecimal`
- Calculations preserve full decimal precision
- FLOOR rounding to whole dollars for point calculation

### 2. Parameter-Driven Routing
```
GET /api/v1/rewards
  → startDate + endDate → Date range query
  → customerId (no dates) → Single customer, N months
  → Neither → All customers, N months
```

### 3. Validation at Controller Level
All parameter validation happens in controller before service method call.

### 4. Private calculatePoints()
The `calculatePoints()` method is intentionally **private** - it's an internal implementation detail, not part of the public API.

---

## 📝 Sample Responses

### Single Customer Response
```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "email": "alice@example.com",
  "membershipTier": "GOLD",
  "periodStart": "2024-02-04",
  "periodEnd": "2024-05-04",
  "monthsCovered": 3,
  "totalTransactions": 5,
  "totalRewardPoints": 365,
  "monthlyBreakdown": [
    {
      "year": 2024,
      "month": 5,
      "monthName": "MAY",
      "monthlyPoints": 250,
      "transactions": [
        {
          "transactionId": "T003",
          "transactionDate": "2024-04-24",
          "amount": 200.00,
          "pointsEarned": 250,
          "description": "Fashion Retail"
        }
      ]
    }
  ]
}
```

### Error Response (400 Bad Request)
```json
{
  "status": 400,
  "message": "months must be between 1 and 36",
  "timestamp": "2024-05-04T10:30:00"
}
```

### Error Response (404 Not Found)
```json
{
  "status": 404,
  "message": "Customer not found: UNKNOWN",
  "timestamp": "2024-05-04T10:30:00"
}
```

---

## 🔧 Configuration

**File:** `src/main/resources/application.properties`

```properties
spring.application.name=rewards-api
server.port=8080
logging.level.com.retailer.rewards=DEBUG
```

### H2 Database

The API now uses Spring Data JPA with an in-memory H2 database.

- H2 console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:rewardsdb`
- Username: `sa`
- Password: empty

Seed customers and transactions are inserted at startup when the database is empty.

### Resilience4j Fallback

Rewards service methods are protected by the `rewardsService` circuit breaker. If the repository/database layer is unavailable, customer-level reward calls return an empty rewards response for the requested period and all-customer calls return an empty list. Business exceptions such as unknown customers are still propagated.

### Docker

```bash
mvn clean package
docker build -t rewards-api:2.0.0 .
docker run --rm -p 8080:8080 rewards-api:2.0.0
```

### Kubernetes

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl port-forward service/rewards-api 8080:8080
```

Health probes use:

```text
/actuator/health/readiness
/actuator/health/liveness
```

---

## 📦 Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

**Testing:**
- JUnit 5
- Mockito
- AssertJ
- Spring Test

---

## ✨ Key Achievements

✅ Single endpoint with intelligent routing
✅ BigDecimal for precise financial calculations
✅ FLOOR rounding for "per dollar" requirement
✅ All validations at controller level
✅ Private calculatePoints() method
✅ Comprehensive test coverage
✅ Clear, maintainable code
✅ Professional error handling

---

## 🎓 How to Use

### Development
```bash
mvn spring-boot:run
```

### Build for Production
```bash
mvn clean package
java -jar target/rewards-api-*.jar
```

### Run Tests
```bash
mvn clean test
```

---

## 📞 Endpoints Quick Reference

```
GET /api/v1/rewards?customerId=C001                           → Single customer
GET /api/v1/rewards?customerId=C001&months=6                  → Single customer (6 months)
GET /api/v1/rewards?customerId=C001&startDate=...&endDate=... → Date range
GET /api/v1/rewards                                           → All customers
GET /api/v1/rewards?months=12                                 → All customers (12 months)
```

---

## 🚀 Ready to Deploy!

All code is:
- ✅ Production-ready
- ✅ Fully tested
- ✅ Well-documented
- ✅ Best practices followed

**Happy coding!** 🎉
