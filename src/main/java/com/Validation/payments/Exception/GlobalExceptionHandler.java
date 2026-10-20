package com.Validation.payments.Exception;


import com.Validation.payments.pojo.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            PaymentValidationException ex) {

        log.error("Validation error: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getErrorMessage())
                .build();

        return new ResponseEntity<>(body, ex.getHttpStatus());
    }


    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<Map<String, String>> handleCircuitBreakerOpen(
            CallNotPermittedException ex) {

        Map<String, String> response = Map.of(
                "message",
                "Payment provider is temporarily unavailable. Please try again later."
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

}