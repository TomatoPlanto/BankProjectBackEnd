package com.banking.models.dto.response;

import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AccountShortResponseDTO {
    private UUID accountId;
    private UUID userId;
    private String ownerName;
    private String iban;
    private AccountType accountType;
}
