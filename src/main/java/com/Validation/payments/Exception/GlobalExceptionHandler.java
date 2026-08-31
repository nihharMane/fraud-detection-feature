package com.Validation.payments.Exception;


import com.Validation.payments.pojo.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
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
}