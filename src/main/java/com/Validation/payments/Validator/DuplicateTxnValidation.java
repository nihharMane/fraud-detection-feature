package com.Validation.payments.Validator;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.MerchantPaymentRequestEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.repository.interfaces.MerchantPaymentRequestRepository;
import com.Validation.payments.service.BusinessValidator;
import com.Validation.payments.service.RedisService;
import com.Validation.payments.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
public class DuplicateTxnValidation implements BusinessValidator {

    private final MerchantPaymentRequestRepository repository;
    private final JsonUtil jsonUtil;
    private final RedisService redisService;

    @Override
    public void validate(PaymentRequest paymentRequest) {

        log.info("Validating Duplicate Transaction for PaymentRequest: {}", paymentRequest);

        // 1. Get transaction reference
        String txnRef = paymentRequest.getPayment().getMerchantTxnRef();

        // 2. Create Redis key
        String redisKey = "fraud:txn:" + txnRef;

        // 3. Check Redis first
        String existingTxn = redisService.get(redisKey);

        if (existingTxn != null) {

            log.warn("Duplicate transaction detected in Redis: {}", txnRef);

            throw new PaymentValidationException(
                    ErrorCode.DUPLICATE_TRANSACTION.getCode(),
                    ErrorCode.DUPLICATE_TRANSACTION.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 4. Create database entity
        MerchantPaymentRequestEntity entity = new MerchantPaymentRequestEntity();

        entity.setEndUserID(paymentRequest.getUser().getEndUserID());
        entity.setMerchantTxnReference(txnRef);
        entity.setCreationDate(LocalDateTime.now());

        String json = jsonUtil.convertObjectToJson(paymentRequest);

        entity.setTransactionRequest(json);

        log.info("JSON String: {}", json);

        // 5. Save transaction to MySQL
        int pkId = repository.saveMerchantPaymentRequestValidation(entity);

        // 6. Handle database duplicate/failure
        if (pkId == -1) {

            log.error("Failed to Save Merchant Payment Request Validation");

            throw new PaymentValidationException(
                    ErrorCode.DUPLICATE_TRANSACTION.getCode(),
                    ErrorCode.DUPLICATE_TRANSACTION.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 7. Save transaction reference in Redis
        redisService.save(redisKey, txnRef);

        log.info("Transaction reference stored in Redis: {}", txnRef);

        log.info(
                "Merchant Payment Request Validation saved successfully with ID: {}",
                pkId
        );
    }
}