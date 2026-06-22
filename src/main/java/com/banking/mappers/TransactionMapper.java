package com.banking.mappers;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;

public class TransactionMapper {
    public static TransactionResponseDTO toDTO(Transaction trans) {
        Account from = trans.getFromAccount();
        Account to = trans.getToAccount();

        return TransactionResponseDTO.builder()
                .transactionId(trans.getTransactionId())
                .fromAccount(from == null ? null : AccountMapper.toShortDTO(from))
                .toAccount(to == null ? null : AccountMapper.toShortDTO(to))
                .amount(trans.getAmount())
                .type(trans.getType())
                .description(trans.getDescription())
                .createdAt(trans.getCreatedAt())
                .build();
    }
}
