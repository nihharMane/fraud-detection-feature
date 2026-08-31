package com.Validation.payments.service;


import com.Validation.payments.pojo.PaymentRequest;

public interface PaymentService {
    String ValidateAndCreatePayment(PaymentRequest paymentRequest, String hmacSignature);
}
