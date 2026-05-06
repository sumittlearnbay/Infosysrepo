package com.retailer.rewards;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestConstants {

    public static final String CUSTOMER_ID = "C001";
    public static final String CUSTOMER_ID_SHORT = "C1";
    public static final String INVALID_ID = "INVALID";
    public static final String NEW_CUSTOMER_ID = "C004";
    public static final String CUSTOMER_NAME = "Alice Johnson";
    public static final String TEST_CUSTOMER_NAME = "Test User";
    public static final String UPDATED_CUSTOMER_NAME = "Updated Name";
    public static final String DUPLICATE_CUSTOMER_NAME = "Duplicate";
    public static final String CUSTOMER_EMAIL = "test@mail.com";
    public static final String MEMBERSHIP_TIER = "GOLD";

    public static final String TRANSACTION_ID = "T001";
    public static final String NEW_TRANSACTION_ID = "T100";
    public static final String INVALID_TRANSACTION_ID = "T101";
    public static final String MOCK_TRANSACTION_ID_1 = "T1";
    public static final String MOCK_TRANSACTION_ID_2 = "T2";
    public static final String MOCK_DESCRIPTION_1 = "Test1";
    public static final String MOCK_DESCRIPTION_2 = "Test2";

    public static final int DEFAULT_MONTHS = 3;
    public static final int CUSTOM_MONTHS = 5;
    public static final int MIN_INVALID_MONTHS = 0;
    public static final int MAX_INVALID_MONTHS = 37;
    public static final int SEEDED_CUSTOMER_COUNT = 3;
    public static final int SEEDED_TRANSACTION_COUNT = 7;
    public static final int ZERO_COUNT = 0;
    public static final int CUSTOMER_COUNT_AFTER_ADD = 4;
    public static final int CUSTOMER_COUNT_AFTER_DELETE = 2;
    public static final int TRANSACTION_COUNT_AFTER_ADD = 8;
    public static final int TRANSACTION_COUNT_AFTER_DELETE = 6;
    public static final int SINGLE_RESULT_COUNT = 1;
    public static final int MOCK_LIST_SIZE = 2;

    public static final BigDecimal TRANSACTION_AMOUNT = new BigDecimal("100.00");
    public static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-10");
    public static final BigDecimal MOCK_AMOUNT_1 = new BigDecimal("120");
    public static final BigDecimal MOCK_AMOUNT_2 = new BigDecimal("80");
    public static final BigDecimal EXPECTED_CUSTOMER_REWARD_POINTS = new BigDecimal("365");

    public static final LocalDate RANGE_START = LocalDate.of(2024, 1, 1);
    public static final LocalDate RANGE_END = LocalDate.of(2024, 2, 1);
    public static final LocalDate LATER_RANGE_START = LocalDate.of(2024, 2, 1);
    public static final LocalDate EARLIER_RANGE_END = LocalDate.of(2024, 1, 1);

    public static final String INVALID_DATE_RANGE_MESSAGE = "Invalid date range";
    public static final String BAD_INPUT_MESSAGE = "Bad input";
    public static final String GENERIC_EXCEPTION_MESSAGE = "Unexpected";
    public static final String GENERIC_ERROR_RESPONSE = "An unexpected error occurred";
    public static final String CUSTOMER_ID_PARAM = "customerId";
    public static final String MONTHS_PARAM = "months";
    public static final String START_DATE_PARAM = "startDate";
    public static final String END_DATE_PARAM = "endDate";
    public static final String STRING_TYPE = "String";
    public static final String NON_NUMERIC_VALUE = "abc";
    public static final String REWARDS_API_PATH = "/api/v1/rewards";
    public static final String JSON_CUSTOMER_ID = "$.customerId";
    public static final String JSON_CUSTOMER_NAME = "$.customerName";
    public static final String JSON_TOTAL_TRANSACTIONS = "$.totalTransactions";
    public static final String JSON_ROOT = "$";

    private TestConstants() {
    }
}
