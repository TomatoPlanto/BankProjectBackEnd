package com.banking.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class GetAccountTransactionsRequestDTO {
    @NotNull(message = "Account id is required")
    private UUID accountId;

    @NotBlank(message = "Page number is required")
    @Min(value = 1, message = "Page number must be more than 0")
    private int pageNumber;

    @NotBlank(message = "Transactions per page is required")
    @Min(value = 1, message = "Transactions per page must be more than 0")
    private int transactionsPerPage;
}
