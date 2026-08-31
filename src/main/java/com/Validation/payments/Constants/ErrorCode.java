package com.Validation.payments.Constants;

public enum ErrorCode {


    USER_REQUIRED(10001, "User object is required"),
    USER_ID_REQUIRED(10002, "User ID must not be empty"),
    FIRST_NAME_REQUIRED(10003, "First name must not be empty"),
    LAST_NAME_REQUIRED(10004, "Last name must not be empty"),
    EMAIL_REQUIRED(10005, "Email must not be empty"),
    INVALID_EMAIL(10006, "Email must be valid"),
    MOBILE_REQUIRED(10007, "Mobile number must not be empty"),


    PAYMENT_REQUIRED(10008, "Payment object is required"),

    CURRENCY_REQUIRED(10009, "Currency must not be empty"),
    AMOUNT_INVALID(10010, "Amount must be greater than 0"),
    BRAND_NAME_REQUIRED(10011, "Brand name must not be empty"),
    LOCALE_REQUIRED(10012, "Locale must not be empty"),
    COUNTRY_REQUIRED(10013, "Country must not be empty"),
    TXN_REF_REQUIRED(10014, "Transaction reference must not be empty"),
    PAYMENT_METHOD_REQUIRED(10015, "Payment method must not be empty"),
    PROVIDER_REQUIRED(10016, "Provider must not be empty"),
    PAYMENT_TYPE_REQUIRED(10017, "Payment type must not be empty"),

    SUCCESS_URL_BLANK(10018, "Success URL must not be empty"),
    SUCCESS_URL_INVALID(10019, "Success URL must be a valid URL"),
    CANCEL_URL_BLANK(10020, "Cancel URL must not be empty"),
    CANCEL_URL_INVALID(10021, "Cancel URL must be a valid URL"),

    LINE_ITEMS_EMPTY(10022, "Line items must not be empty"),

    PRODUCT_NAME_BLANK(10023, "Product name must not be empty"),
    CURRENCY_BLANK(10024, "Currency must not be empty"),
    CURRENCY_INVALID(10025, "Currency must be a valid 3-letter ISO code"),
    UNIT_AMOUNT_INVALID(10026, "Unit amount must be greater than 0"),
    QUANTITY_INVALID(10027, "Quantity must be at least 1"),

    STRIPE_ERROR(20001, "Stripe error"),

    INTERNAL_ERROR(90001, "Internal server error"),
    FIRSTNAME_CONTAIN_HELLO(10028, "First name must not be hello" ),
    DUPLICATE_TRANSACTION(10029, "Duplicate transaction detected"),
    FAILED_TO_SAVE(10030, "Failed to save merchant payment request validation"),
    INVALID_HMAC(10031, "Invalid HMAC signature"),
    MISSING_HMAC(10032, "Missing HMAC signature"),
    HIGH_RISK_TRANSACTION_BLOCKED(10033, "Transaction blocked due to high fraud risk"),
    RECORD_NOT_FOUND(10034, "Requested record was not found");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}