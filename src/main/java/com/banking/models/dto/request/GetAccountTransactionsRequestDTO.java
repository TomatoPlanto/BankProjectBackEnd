package com.banking.models.dto.request;

import jakarta.validation.constraints.Min;
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

    @Min(value = 0, message = "Page number must be 0 or greater")
    private int pageNumber;

    @Min(value = 1, message = "Must request at least 1 transaction per page")
    private int transactionsPerPage;

    private String sorting;

    private boolean sortingOrder;
}