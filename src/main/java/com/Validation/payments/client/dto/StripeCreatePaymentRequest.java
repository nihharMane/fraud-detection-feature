package com.Validation.payments.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class StripeCreatePaymentRequest {

    private String successUrl;
    private String cancelUrl;
    private List<StripeLineItem> lineItems;
}