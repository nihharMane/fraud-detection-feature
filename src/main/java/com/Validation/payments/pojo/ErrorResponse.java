package com.Validation.payments.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private int errorCode;
    private String errorMessage;
}
