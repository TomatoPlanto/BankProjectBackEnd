package com.banking.services;

import com.banking.exceptions.*;
import com.banking.mappers.AccountMapper;
import com.banking.mappers.TransactionMapper;
import com.banking.models.dto.request.TransferBetweenAccountsRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import com.banking.models.enums.TransactionType;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.Interface.ITransactionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID transactionId){
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + transactionId));
        return TransactionMapper.toDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionResponseDTO transferBetweenAccounts(TransferBetweenAccountsRequestDTO request){
        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) throw new AccountNotFoundException("Account not found with id: " + request.getFromAccountId());
        Account fromAccount = fromAccountWrap.get();

        var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
        if(toAccountWrap.isEmpty()) throw new AccountNotFoundException("Account not found with id: " + request.getToAccountId());
        Account toAccount = toAccountWrap.get();

        if(toAccount.getStatus() != AccountStatus.ACTIVE || fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("To and from accounts must be active");
        }

        if(fromAccount.getAccountType() == AccountType.SAVINGS || toAccount.getAccountType() == AccountType.SAVINGS){
            if(fromAccount.getUser().getUserId() != toAccount.getUser().getUserId()){
                throw new TransactionFromSavingAccountException("User can only transfer funds from savings account to their other account.");
            }
        }

        if(fromAccount.getTransferLimit().compareTo(request.getTransferAmount()) < 0) {
            throw new TransferAmountExceedLimitException("The transfer amount (" + request.getTransferAmount() + ") exceeds account limit (" + fromAccount.getTransferLimit() + ")");
        }

        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getTransferAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds in account to perform the transaction");
        }

        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description(request.getDescription())
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(trans);

        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getTransferAmount()));

        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    @Override
    public List<TransactionResponseDTO> getAllAccountTransactions(UUID accountId){
        return transactionRepository.findBy(userId)
                .stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());
    }
}
