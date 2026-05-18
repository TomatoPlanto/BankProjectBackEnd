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
public class TransferBetweenAccountsRequestDTO {
    @NotNull(message = "From account id is required")
    private UUID fromAccountId;

    @NotNull(message = "To account id is required")
    private UUID toAccountId;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer limit must be positive and higher than 0.01")
    private BigDecimal transferAmount;

    @NotNull(message = "Description is required")
    private String description;
}
