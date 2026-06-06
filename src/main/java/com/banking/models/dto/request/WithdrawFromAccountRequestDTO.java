package com.banking.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WithdrawFromAccountRequestDTO {
    @NotNull(message = "From account id is required")
    private UUID fromAccountId;

    @NotNull(message = "Withdraw amount is required")
    @DecimalMin(value = "0.01", message = "Withdraw amount must be positive and higher than 0.01")
    private BigDecimal withdrawAmount;
}
