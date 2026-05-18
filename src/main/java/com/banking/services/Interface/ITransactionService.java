package com.banking.services.Interface;

import com.banking.models.dto.request.TransferBetweenAccountsRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;

import java.util.UUID;

public interface ITransactionService {
    TransactionResponseDTO getTransactionById(UUID transactionId);
    TransactionResponseDTO transferBetweenAccounts(TransferBetweenAccountsRequestDTO request);
}
