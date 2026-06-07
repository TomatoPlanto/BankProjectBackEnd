package com.banking.models.dto.request;

import com.banking.models.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateAccountRequestDTO {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.0", message = "Daily limit must be positive")
    private BigDecimal dailyLimit;

    @NotNull(message = "Transfer limit is required")
    @DecimalMin(value = "0.0", message = "Transfer limit must be positive")
    private BigDecimal transferLimit;

    @NotNull(message = "Absolute minimum is required")
    private BigDecimal absoluteMinimum;
}