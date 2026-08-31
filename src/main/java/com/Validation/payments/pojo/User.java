package com.Validation.payments.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class User {

    @NotBlank(message = "USER_ID_REQUIRED")
    @Size(min = 3, max = 50, message = "USER_ID_INVALID")
    private String endUserID;

    @NotBlank(message = "FIRST_NAME_REQUIRED")
    @Pattern(regexp = "^[A-Za-z]+$", message = "FIRST_NAME_INVALID")
    private String firstname;

    @NotBlank(message = "LAST_NAME_REQUIRED")
    @Pattern(regexp = "^[A-Za-z]+$", message = "LAST_NAME_INVALID")
    private String lastname;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "MOBILE_REQUIRED")
    @Pattern(
            regexp = "^\\+?[1-9][0-9]{7,14}$",
            message = "MOBILE_INVALID"
    )
    private String mobilePhone;
}