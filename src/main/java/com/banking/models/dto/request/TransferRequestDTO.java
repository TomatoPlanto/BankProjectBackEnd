package com.banking.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransferRequestDTO {
    @Nullable
    private UUID fromAccountId;

    @Nullable
    private UUID toAccountId;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer limit must be positive and higher than 0.01")
    private BigDecimal transferAmount;

    @NotNull(message = "Description is required")
    private String description;
}
