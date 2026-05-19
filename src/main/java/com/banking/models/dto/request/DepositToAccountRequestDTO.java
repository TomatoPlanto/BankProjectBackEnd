package com.banking.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DepositToAccountRequestDTO {
    @NotNull(message = "To account id is required")
    private UUID toAccountId;

    @NotNull(message = "Deposit amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be positive and higher than 0.01")
    private BigDecimal depositAmount;
}
