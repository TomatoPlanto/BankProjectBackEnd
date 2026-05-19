package com.banking.services.Interface;

import com.banking.models.dto.request.DepositToAccountRequestDTO;
import com.banking.models.dto.request.TransferBetweenAccountsRequestDTO;
import com.banking.models.dto.request.WithdrawFromAccountRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ITransactionService {
    TransactionResponseDTO getTransactionById(UUID transactionId);
    TransactionResponseDTO transferBetweenAccounts(TransferBetweenAccountsRequestDTO request);
    List<TransactionResponseDTO> getAllAccountTransactions(UUID accountId);
    TransactionResponseDTO withdrawFromAccount(WithdrawFromAccountRequestDTO request);
    TransactionResponseDTO depositToAccount(DepositToAccountRequestDTO request);
}
