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
public class AtmLoginResponseDTO {
    private String token;
    private UUID accountId;
    private String iban;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private BigDecimal transferLimit;
    private BigDecimal absoluteMinimum;
    private AccountStatus status;
}