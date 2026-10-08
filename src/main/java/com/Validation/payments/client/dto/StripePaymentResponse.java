package com.Validation.payments.client.dto;

import lombok.Data;

@Data
public class StripePaymentResponse {

    private String stripeSessionId;
    private String hostedPageUrl;
}