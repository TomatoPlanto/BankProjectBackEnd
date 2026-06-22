package com.banking.models.dto.request;

import com.banking.models.enums.AccountStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// all fields optional - only the ones sent get updated
@Getter
@Setter
public class UpdateAccountRequestDTO {

    @DecimalMin(value = "0.0", message = "Daily limit must be positive")
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.0", message = "Transfer limit must be positive")
    private BigDecimal transferLimit;

    private BigDecimal absoluteMinimum;

    private AccountStatus status;
}