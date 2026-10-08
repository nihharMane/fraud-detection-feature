package com.Validation.payments.client.dto;

import lombok.Data;

@Data
public class StripeLineItem {

    private String currency;
    private String productName;
    private int unitAmount;
    private int quantity;
}