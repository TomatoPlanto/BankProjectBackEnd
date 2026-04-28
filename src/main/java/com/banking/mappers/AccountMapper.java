package com.banking.mappers;

import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.entities.Account;

public class AccountMapper {

    public static AccountResponseDTO toDTO(Account account) {
        return AccountResponseDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUser().getUserId())
                .iban(account.getIban())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .dailyLimit(account.getDailyLimit())
                .transferLimit(account.getTransferLimit())
                .absoluteMinimum(account.getAbsoluteMinimum())
                .status(account.getStatus())
                .build();
    }
}