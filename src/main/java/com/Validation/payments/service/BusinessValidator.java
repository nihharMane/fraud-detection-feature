package com.Validation.payments.service;

import com.Validation.payments.pojo.PaymentRequest;

public interface BusinessValidator {
    public void validate(PaymentRequest paymentRequest) ;
}
