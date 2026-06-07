package com.banking.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtmLoginRequestDTO {

    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "\\d{9}", message = "BSN must contain only digits") // need to alter this one, just as a reference fo now
    private String iban;

    @NotBlank(message = "Pin is required")
    private int pin;
}
