package com.banking.services;

import com.banking.exceptions.*;
import com.banking.mappers.AccountMapper;
import com.banking.mappers.TransactionMapper;
import com.banking.models.dto.request.DepositToAccountRequestDTO;
import com.banking.models.dto.request.TransferBetweenAccountsRequestDTO;
import com.banking.models.dto.request.WithdrawFromAccountRequestDTO;
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
        var transaction = transactionRepository.findByTransactionId(transactionId);
        if(transaction.isEmpty()) throw new TransactionNotFoundException("Transaction not found with id: " + transactionId);

        return TransactionMapper.toDTO(transaction.get());
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

        if(toAccount.getAccountId() == fromAccount.getAccountId()){
            throw new RuntimeException("Sender and receiver accounts are the same account");
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
        var account = accountRepository.findByAccountId(accountId);
        if(account.isEmpty()) throw new AccountNotFoundException("Account with id: " + accountId + " not found");

        return transactionRepository.findAllAccountTransactions(accountId)
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponseDTO withdrawFromAccount(WithdrawFromAccountRequestDTO request){
        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) throw new AccountNotFoundException("Account with id: " + request.getFromAccountId() + " not found");
        Account fromAccount = fromAccountWrap.get();

        if(fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        if(fromAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Withdraw from savings account is forbidden");
        }

        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getWithdrawAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds for the withdrawal");
        }

        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(null)
                .amount(request.getWithdrawAmount())
                .description("Withdrawal")
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(trans);

        try{
            accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getWithdrawAmount()));
        }
        catch (Exception ex){
            throw new NotEnoughFundsException(ex.getMessage());
        }

        return TransactionMapper.toDTO(trans);
    }

    @Override
    @Transactional
    public TransactionResponseDTO depositToAccount(DepositToAccountRequestDTO request){
        var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
        if(toAccountWrap.isEmpty()) throw new AccountNotFoundException("Account with id: " + request.getToAccountId() + " not found");
        Account toAccount = toAccountWrap.get();

        if(toAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        if(toAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Deposit to savings account is forbidden");
        }

        Transaction trans = Transaction.builder()
                .fromAccount(null)
                .toAccount(toAccount)
                .amount(request.getDepositAmount())
                .description("Deposit")
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(trans);

        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getDepositAmount()));

        return TransactionMapper.toDTO(trans);
    }
}
