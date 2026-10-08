package com.Validation.payments.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentValidationResponse {

    private String message;
    private String merchantTxnRef;
    private String decision;
    private String stripeSessionId;
    private String hostedPageUrl;
}