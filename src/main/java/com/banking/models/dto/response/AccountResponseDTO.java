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
public class AccountResponseDTO {

    private UUID accountId;
    private UUID userId;
    private String iban;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal dailyLimit;
    private BigDecimal transferLimit;
    private BigDecimal absoluteMinimum;
    private AccountStatus status;
    private int pin;
}