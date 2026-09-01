package com.Validation.payments.repository.impl;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.MerchantPaymentRequestEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.repository.interfaces.MerchantPaymentRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MerchantPaymentRequestRepositoryImpl implements MerchantPaymentRequestRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int saveMerchantPaymentRequestValidation(MerchantPaymentRequestEntity entity) {

        log.info("Saving MerchantPaymentRequestValidation for txnRef: {}",
                entity.getMerchantTxnReference());

        String sql = """
    INSERT INTO merchant_payment_request
    (end_userid, merchant_txn_reference, transaction_request, creation_date)
    VALUES
    (:endUserID, :txnRef, :txnRequest, :creationDate)
    """;

        Map<String, Object> params = new HashMap<>();
        params.put("endUserID", entity.getEndUserID());
        params.put("txnRef", entity.getMerchantTxnReference());
        params.put("txnRequest", entity.getTransactionRequest());
        params.put("creationDate", entity.getCreationDate());

        try {
            //  KeyHolder to capture auto-generated ID
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder);

            Number generatedId = keyHolder.getKey();

            if (generatedId != null) {
                log.info("Insert successful. Generated ID: {}", generatedId.intValue());
                return generatedId.intValue(); //  return PK
            } else {
                log.warn("Insert succeeded but no ID returned for txnRef: {}",
                        entity.getMerchantTxnReference());
                throw new PaymentValidationException(
                        ErrorCode.FAILED_TO_SAVE.getCode(),
                        ErrorCode.FAILED_TO_SAVE.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

        } catch (org.springframework.dao.DuplicateKeyException ex) {
            //  Handle duplicate key
            log.error("Duplicate transaction detected for txnRef: {}",
                    entity.getMerchantTxnReference(), ex);
            return -1; // duplicate case

        } catch (Exception ex) {
            log.error("Error inserting merchant payment request", ex);
            throw new RuntimeException("Database error while inserting payment request", ex);
        }


    }
}