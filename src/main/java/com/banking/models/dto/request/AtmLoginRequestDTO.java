package com.banking.models.dto.request;

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

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits")
    private String pin;

}
