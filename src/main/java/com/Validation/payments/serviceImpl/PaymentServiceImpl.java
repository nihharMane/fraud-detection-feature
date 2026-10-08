package com.Validation.payments.serviceImpl;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Constants.ValidatorRuleEnum;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.client.StripeProviderClient;

import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.service.BusinessValidator;
import com.Validation.payments.service.PaymentService;
import com.Validation.payments.client.StripePaymentMapper;
import com.Validation.payments.client.StripeProviderClient;
import com.Validation.payments.client.dto.StripeCreatePaymentRequest;
import com.Validation.payments.client.dto.StripePaymentResponse;
import com.Validation.payments.pojo.PaymentValidationResponse;
import com.Validation.payments.util.HmacSHA256Util;
import com.Validation.payments.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${validator.rule.name}")
    private String validatorRuleName;

    private final JsonUtil jsonUtil;
    private final ApplicationContext applicationContext;
    private final StripeProviderClient stripeProviderClient;
    private final StripePaymentMapper stripePaymentMapper;

    @Override
    public PaymentValidationResponse ValidateAndCreatePayment(
            PaymentRequest paymentRequest,
            String headerHmacSignature) {

        if (headerHmacSignature == null ||
                headerHmacSignature.isEmpty()) {

            log.error("Hmac Signature is missing in the request header");

            throw new PaymentValidationException(
                    ErrorCode.MISSING_HMAC.getCode(),
                    ErrorCode.MISSING_HMAC.getMessage(),
                    HttpStatus.UNAUTHORIZED
            );
        }

        String[] rules = validatorRuleName.split(",");

        for (String rule : rules) {

            log.info("Applying Validator rule: {}", rule);

            Optional<Class<? extends BusinessValidator>> validatorClass =
                    ValidatorRuleEnum.getValidatorClassByRule(rule.trim());

            if (validatorClass.isEmpty()) {
                log.info("Validator class not found: {}", rule);
                continue;
            }

            BusinessValidator businessValidator =
                    applicationContext.getBean(validatorClass.get());

            if (businessValidator == null) {
                log.warn(
                        "BusinessValidator not found: {}",
                        validatorClass.get().getName()
                );
                continue;
            }

            businessValidator.validate(paymentRequest);

            log.info(
                    "Validator rule {} applied successfully",
                    rule
            );
        }

        /*
         * IMPORTANT:
         * We reach this point only when all configured
         * validation rules have passed.
         */

        log.info(
                "All fraud validation rules passed for transaction: {}",
                paymentRequest.getPayment().getMerchantTxnRef()
        );

        /*
         * Only Stripe transactions go to Stripe Provider.
         */
        if ("STRIPE".equalsIgnoreCase(
                paymentRequest.getPayment().getProvider())) {

            StripeCreatePaymentRequest stripeRequest =
                    stripePaymentMapper.map(
                            paymentRequest.getPayment()
                    );

            log.info(
                    "Calling Stripe Provider Service for transaction: {}",
                    paymentRequest.getPayment().getMerchantTxnRef()
            );

            StripePaymentResponse stripeResponse =
                    stripeProviderClient.createPayment(stripeRequest);

            log.info(
                    "Stripe Checkout Session created: {}",
                    stripeResponse.getStripeSessionId()
            );

            return new PaymentValidationResponse(
                    "Payment validated and Stripe checkout session created",
                    paymentRequest.getPayment().getMerchantTxnRef(),
                    "APPROVED",
                    stripeResponse.getStripeSessionId(),
                    stripeResponse.getHostedPageUrl()
            );
        }

        /*
         * Razorpay is currently not integrated.
         */
        return new PaymentValidationResponse(
                "Payment validated successfully",
                paymentRequest.getPayment().getMerchantTxnRef(),
                "APPROVED",
                null,
                null
        );
    }
}