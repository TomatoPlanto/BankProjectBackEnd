package com.banking.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateAccountLimitsRequestDTO {

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.0", message = "Daily limit must be positive")
    private BigDecimal dailyLimit;

    @NotNull(message = "Transfer limit is required")
    @DecimalMin(value = "0.0", message = "Transfer limit must be positive")
    private BigDecimal transferLimit;

    @NotNull(message = "Absolute minimum is required")
    private BigDecimal absoluteMinimum;
}