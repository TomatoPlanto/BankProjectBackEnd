package com.banking.models.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtmLoginRequestDTO {

    @NotBlank(message = "IBAN is required")
    @Email(message = "IBAN must be valid")
    private String iban;

    @NotBlank(message = "Pin is required")
    private int pin;
}
