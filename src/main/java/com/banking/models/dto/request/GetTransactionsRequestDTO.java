package com.banking.models.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.UUID;

@Getter
@Setter
@Builder
public class GetTransactionsRequestDTO {
    @Nullable
    private UUID accountId;

    @Min(value = 0, message = "Page number must be more than 0")
    private int pageNumber;

    @Min(value = 1, message = "Transactions per page must be more than 0")
    private int transactionsPerPage;

    @NotNull(message = "Sorting is required")
    private String sorting;

    @NotNull(message = "Sorting order is required")
    private boolean sortingOrder;
}
