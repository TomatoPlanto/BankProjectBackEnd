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
        // Get from account
        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) throw new AccountNotFoundException("From account not found");
        Account fromAccount = fromAccountWrap.get();

        // Get to account
        var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
        if(toAccountWrap.isEmpty()) throw new AccountNotFoundException("To account not found");
        Account toAccount = toAccountWrap.get();

        // Check if accounts are active
        if(toAccount.getStatus() != AccountStatus.ACTIVE || fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("To and from accounts must be active");
        }

        // Check if accounts are not the same
        if(toAccount.getAccountId() == fromAccount.getAccountId()){
            throw new RuntimeException("To and from accounts must not be the same");
        }

        // Check if from or to accounts are savings. If they are, check if they are owned by the same person
        if(fromAccount.getAccountType() == AccountType.SAVINGS || toAccount.getAccountType() == AccountType.SAVINGS){
            if(fromAccount.getUser().getUserId() != toAccount.getUser().getUserId()){
                throw new TransactionFromSavingAccountException("User can only transfer funds from savings account to their other account.");
            }
        }

        // Check from transfer limit
        if(fromAccount.getTransferLimit().compareTo(request.getTransferAmount()) < 0) {
            throw new TransferAmountExceedLimitException("The transfer amount exceeds account limit");
        }

        // Check fromAccount daily limit
        if(fromAccount.getDailyLimit().compareTo(fromAccount.getTodayChange().add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("From account daily limit exceeded");
        }

        // Check toAccount daily limit
        if(toAccount.getDailyLimit().compareTo(toAccount.getTodayChange().add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("To account daily limit exceeded");
        }

        // Check if from account have enough funds to perform the transaction
        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getTransferAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds in account to perform the transaction");
        }

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description(request.getDescription())
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Save transaction
        transactionRepository.save(trans);

        // Remove money from fromAccount
        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getTransferAmount()));
        // Add money to toAccount
        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getTransferAmount()));

        // Add to fromAccount's todayChange
        accountRepository.setTodayChange(fromAccount.getAccountId(), fromAccount.getTodayChange().add(request.getTransferAmount()));
        // Add to toAccount's todayChange
        accountRepository.setTodayChange(toAccount.getAccountId(), toAccount.getTodayChange().add(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    @Override
    public List<TransactionResponseDTO> getAllAccountTransactions(UUID accountId){
        var account = accountRepository.findByAccountId(accountId);
        if(account.isEmpty()) throw new AccountNotFoundException("Account not found");

        return transactionRepository.findAllAccountTransactions(accountId)
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponseDTO withdrawFromAccount(WithdrawFromAccountRequestDTO request){
        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) throw new AccountNotFoundException("Account not found");
        Account fromAccount = fromAccountWrap.get();

        // Check if account is active
        if(fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        // Check if account is savings
        if(fromAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Withdraw from savings account is forbidden");
        }

        // Check transfer limit
        if(fromAccount.getTransferLimit().compareTo(request.getWithdrawAmount()) < 0){
            throw new TransferAmountExceedLimitException("Withdraw amount exceeds transfer limit)");
        }

        // Check fromAccount daily limit
        if(fromAccount.getDailyLimit().compareTo(fromAccount.getTodayChange().add(request.getWithdrawAmount())) < 0){
            throw new DailyLimitExceededException("Account's daily limit exceeded");
        }

        // Check if there is enough funds to perform the withdrawal
        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getWithdrawAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds for the withdrawal");
        }

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(null)
                .amount(request.getWithdrawAmount())
                .description("Withdrawal")
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Add transaction
        transactionRepository.save(trans);

        // Remove money from the account
        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getWithdrawAmount()));

        // Add to account's todayChange
        accountRepository.setTodayChange(fromAccount.getAccountId(), fromAccount.getTodayChange().add(request.getWithdrawAmount()));

        return TransactionMapper.toDTO(trans);
    }

    @Override
    @Transactional
    public TransactionResponseDTO depositToAccount(DepositToAccountRequestDTO request){
        var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
        if(toAccountWrap.isEmpty()) throw new AccountNotFoundException("Account not found");
        Account toAccount = toAccountWrap.get();

        // Check if account is active
        if(toAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        // Check if account is savings
        if(toAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Deposit to savings account is forbidden");
        }

        // Check transfer limit
        if(toAccount.getTransferLimit().compareTo(request.getDepositAmount()) < 0){
            throw new TransferAmountExceedLimitException("Deposit amount exceeds transfer limit");
        }

        // Check account's daily limit
        if(toAccount.getDailyLimit().compareTo(toAccount.getTodayChange().add(request.getDepositAmount())) < 0){
            throw new DailyLimitExceededException("Account's daily limit exceeded");
        }

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(null)
                .toAccount(toAccount)
                .amount(request.getDepositAmount())
                .description("Deposit")
                .type(TransactionType.CUSOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Add transaction
        transactionRepository.save(trans);

        // Add money to account
        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getDepositAmount()));

        // Add to account's todayChange
        accountRepository.setTodayChange(toAccount.getAccountId(), toAccount.getTodayChange().add(request.getDepositAmount()));

        return TransactionMapper.toDTO(trans);
    }
}
