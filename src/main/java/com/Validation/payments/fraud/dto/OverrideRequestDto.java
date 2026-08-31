package com.Validation.payments.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OverrideRequestDto {

    @NotBlank(message = "overrideNote is required")
    private String overrideNote;
}