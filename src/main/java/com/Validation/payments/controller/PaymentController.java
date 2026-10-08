package com.Validation.payments.controller;

import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.pojo.PaymentValidationResponse;
import com.Validation.payments.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentValidationResponse createPayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader(
                    value = "Hmac-Signature",
                    required = false
            )
            String hmacSignature) {

        log.info("Hmac Signature received");

        log.info("Creating Payment...");

        PaymentValidationResponse serviceResponse =
                paymentService.ValidateAndCreatePayment(
                        paymentRequest,
                        hmacSignature
                );

        log.info(
                "Payment processed successfully: {}",
                serviceResponse
        );

        return serviceResponse;
    }
}