package com.Validation.payments.Exception;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Data
@Setter
@Getter
public class PaymentValidationException extends RuntimeException{
private final int errorCode;
private final String errorMessage;
private final HttpStatus httpStatus;

public PaymentValidationException(int errorCode, String errorMessage, HttpStatus httpStatus) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.httpStatus = httpStatus;
}

public int getErrorCode() {
    return errorCode;
}

public String getErrorMessage() {
    return errorMessage;
}

public HttpStatus getHttpStatus() {
    return httpStatus;

}
}
