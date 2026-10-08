package com.Validation.payments.service;

import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.pojo.PaymentValidationResponse;

public interface PaymentService {

    PaymentValidationResponse ValidateAndCreatePayment(
            PaymentRequest paymentRequest,
            String hmacSignature);
}