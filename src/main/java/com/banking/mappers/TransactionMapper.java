package com.banking.mappers;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.dto.response.UserResponseDTO;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;

public class TransactionMapper {
    public static TransactionResponseDTO toDTO(Transaction trans) {
        return TransactionResponseDTO.builder()
                .transactionId(trans.getTransactionId())
                .fromAccountId(trans.getFromAccount().getAccountId())
                .toAccountId(trans.getToAccount().getAccountId())
                .amount(trans.getAmount())
                .type(trans.getType())
                .description(trans.getDescription())
                .createdAt(trans.getCreatedAt())
                .build();
    }
}
