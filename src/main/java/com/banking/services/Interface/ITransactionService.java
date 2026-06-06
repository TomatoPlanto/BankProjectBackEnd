package com.banking.services.Interface;

import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.dto.response.CountResponseDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ITransactionService {
    TransactionResponseDTO getTransactionById(UUID transactionId);
    TransactionResponseDTO transfer(TransferRequestDTO request);
    Page<TransactionResponseDTO> getAccountTransactions(GetAccountTransactionsRequestDTO request);
    CountResponseDTO getAccountTransactionsCount(UUID accountId);
}
