    package com.Validation.payments.controller;

    import com.Validation.payments.pojo.PaymentRequest;
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


        @GetMapping("/health")
        public String health() {
            return "Fraud Detection API is running!";
        }

        @PostMapping
        public String createPayment(@Valid @RequestBody PaymentRequest paymentRequest,
                                    @RequestHeader (value = "Hmac-Signature", required = false)
                                    String hmacSignature) {
            log.info("Hmac Signature Response: " + hmacSignature );

            log.info("Creating Payment..." );
            String serviceResponse= paymentService.ValidateAndCreatePayment(paymentRequest,hmacSignature);
            log.info(" Payment created successfully {}",serviceResponse );
            return serviceResponse;

        }
    }
