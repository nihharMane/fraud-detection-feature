package com.Validation.payments.pojo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class Payment {

    @NotBlank(message = "CURRENCY_REQUIRED")
    @Pattern(regexp = "^[A-Z]{3}$", message = "CURRENCY_INVALID") // ISO code
    private String currency;

    @Min(value = 1, message = "AMOUNT_INVALID")
    private long amount;

    @NotBlank(message = "BRAND_NAME_REQUIRED")
    @Size(min = 2, max = 50, message = "BRAND_NAME_INVALID")
    private String brandName;

    @NotBlank(message = "LOCALE_REQUIRED")
    @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "LOCALE_INVALID") // en-US
    private String locale;

    @NotBlank(message = "COUNTRY_REQUIRED")
    @Pattern(regexp = "^[A-Z]{2}$", message = "COUNTRY_INVALID") // US, IN
    private String country;

    @NotBlank(message = "TXN_REF_REQUIRED")
    @Size(min = 5, max = 50, message = "TXN_REF_INVALID")
    private String merchantTxnRef;

    @NotBlank(message = "PAYMENT_METHOD_REQUIRED")
    @Pattern(regexp = "^(CARD|UPI|APM)$", message = "PAYMENT_METHOD_INVALID")
    private String paymentMethod;

    @NotBlank(message = "PROVIDER_REQUIRED")
    @Pattern(regexp = "^(STRIPE|RAZORPAY)$", message = "PROVIDER_INVALID")
    private String provider;

    @NotBlank(message = "PAYMENT_TYPE_REQUIRED")
    @Pattern(regexp = "^(SALE|AUTH)$", message = "PAYMENT_TYPE_INVALID")
    private String paymentType;

    @NotBlank(message = "SUCCESS_URL_BLANK")
    @Pattern(regexp = "^(http|https)://.*$", message = "SUCCESS_URL_INVALID")
    private String successUrl;

    @NotBlank(message = "CANCEL_URL_BLANK")
    @Pattern(regexp = "^(http|https)://.*$", message = "CANCEL_URL_INVALID")
    private String cancelUrl;

    @NotEmpty(message = "LINE_ITEMS_EMPTY")
    @Valid
    private List<LineItem> lineItems;
}