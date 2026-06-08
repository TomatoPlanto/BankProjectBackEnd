package com.banking.models.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtmLoginRequestDTO {

    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "^NL\\d{2}INHO0\\d{10}$", message = "IBAN must be NLxxINHO0xxxxxxxxx")
    private String iban;

    @Min(value = 1000, message = "PIN must be 4 digits")
    @Max(value = 9999, message = "PIN must be 4 digits")
    private int pin;

}
