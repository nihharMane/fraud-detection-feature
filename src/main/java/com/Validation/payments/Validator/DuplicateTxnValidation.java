package com.Validation.payments.Validator;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.MerchantPaymentRequestEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.repository.interfaces.MerchantPaymentRequestRepository;
import com.Validation.payments.service.BusinessValidator;
import com.Validation.payments.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service

@AllArgsConstructor
public class DuplicateTxnValidation implements BusinessValidator {
    private final MerchantPaymentRequestRepository repository;
    private final JsonUtil jsonUtil;
    @Override
    public void validate(PaymentRequest paymentRequest) {
        log.info("Validating Duplicate Transaction for PaymentRequest: {}", paymentRequest);

        MerchantPaymentRequestEntity entity = new MerchantPaymentRequestEntity();

        entity.setEndUserID(paymentRequest.getUser().getEndUserID());
        entity.setMerchantTxnReference(paymentRequest.getPayment().getMerchantTxnRef());
        entity.setCreationDate(LocalDateTime.now());
       String json= jsonUtil.convertObjectToJson(paymentRequest);
       entity.setTransactionRequest(json);
       log.info("JSON String: {}", json);

        int pkId = repository.saveMerchantPaymentRequestValidation(entity); //TODO
        //int pkId=new Random().nextInt(100);
        if (pkId == -1){
            log.error("Failed to Save Merchant Payment Request Validation");
            throw new PaymentValidationException(
                    ErrorCode.DUPLICATE_TRANSACTION.getCode(),
                     ErrorCode.DUPLICATE_TRANSACTION.getMessage(),

                    HttpStatus.BAD_REQUEST
            );

        }
        log.info(" Merchant Payment Request Validation saved successfully with ID: {}", pkId);



    }
}
