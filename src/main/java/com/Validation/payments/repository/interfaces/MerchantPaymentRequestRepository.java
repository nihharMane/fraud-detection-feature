package com.Validation.payments.repository.interfaces;

import com.Validation.payments.Entity.MerchantPaymentRequestEntity;

import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPaymentRequestRepository   {

    public int saveMerchantPaymentRequestValidation(MerchantPaymentRequestEntity merchantPaymentRequestEntity);

}
