package com.Validation.payments.pojo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LineItem {

    @NotBlank(message = "CURRENCY_BLANK")
    @Pattern(regexp = "^[A-Z]{3}$", message = "CURRENCY_INVALID")
    private String currency;

    @NotBlank(message = "PRODUCT_NAME_BLANK")
    @Size(min = 2, max = 100, message = "PRODUCT_NAME_INVALID")
    private String productName;

    @Min(value = 1, message = "UNIT_AMOUNT_INVALID")
    private long unitAmount;

    @Min(value = 1, message = "QUANTITY_INVALID")
    private int quantity;
}