package com.banking.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateBalanceRequestDTO {

    @NotBlank(message = "IBAN is required")
    @Pattern(regexp = "\\d{9}", message = "BSN must contain only digits") // need to alter this one, just as a reference fo now
    private String iban;

    @NotBlank(message = "Money is required") // idk how to phrase this. Should ask if withdraw and input can be same method
    private BigDecimal balanceUpdate;
}
