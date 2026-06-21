package com.banking.policy;

import com.banking.exceptions.*;
import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import com.banking.models.enums.UserRole;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionPolicy {
    private final TransactionRepository transactionRepository;

    public TransactionPolicy(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void enforceTransferBetweenAccounts(TransferRequestDTO request, Account fromAccount, Account toAccount){
        // Check if accounts are active
        if(toAccount.getStatus() != AccountStatus.ACTIVE || fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Sender and receiver account's must be active");
        }

        // Check if accounts are not the same
        if(toAccount.getAccountId() == fromAccount.getAccountId()){
            throw new RuntimeException("Sender and Receiver account's must not be the same");
        }

        // Check if from or to accounts are savings. If they are, check if they are owned by the same person
        if(fromAccount.getAccountType() == AccountType.SAVINGS || toAccount.getAccountType() == AccountType.SAVINGS){
            if(fromAccount.getUser().getUserId() != toAccount.getUser().getUserId()){
                throw new TransactionFromSavingAccountException("Transfers from/to savings accounts can be performed only by the owner");
            }
        }

        // Check if from account have enough funds to perform the transaction
        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getTransferAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds in sender's account to perform the transaction");
        }

        // Check from transfer limit
        if(fromAccount.getTransferLimit().compareTo(request.getTransferAmount()) < 0) {
            throw new TransferAmountExceedLimitException("The transfer amount exceeds account limit");
        }

        // Check fromAccount daily limit
        if(fromAccount.getDailyLimit().compareTo(getAccountChangeDay(fromAccount.getAccountId()).add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("Sender's account daily limit exceeded");
        }

        // Check toAccount daily limit
        if(toAccount.getDailyLimit().compareTo(getAccountChangeDay(toAccount.getAccountId()).add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("Receiver's account daily limit exceeded");
        }
    }

    public void enforceWithdrawFromAccounts(TransferRequestDTO request, Account fromAccount){
        // Check if account is active
        if(fromAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        // Check if account is savings
        if(fromAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Withdraw from savings account is forbidden");
        }

        // Check if there is enough funds to perform the withdrawal
        if(fromAccount.getAbsoluteMinimum().compareTo(fromAccount.getBalance().subtract(request.getTransferAmount())) > 0){
            throw new NotEnoughFundsException("Not enough funds for the withdrawal");
        }

        // Check transfer limit
        if(fromAccount.getTransferLimit().compareTo(request.getTransferAmount()) < 0){
            throw new TransferAmountExceedLimitException("Withdraw amount exceeds transfer limit");
        }

        // Check fromAccount daily limit
        if(fromAccount.getDailyLimit().compareTo(getAccountChangeDay(fromAccount.getAccountId()).add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("Account's daily limit exceeded");
        }
    }

    public void enforceDepositToAccounts(TransferRequestDTO request, Account toAccount){
        // Check if account is active
        if(toAccount.getStatus() != AccountStatus.ACTIVE){
            throw new AccountNotActiveException("Account is not active");
        }

        // Check if account is savings
        if(toAccount.getAccountType() == AccountType.SAVINGS){
            throw new TransactionFromSavingAccountException("Deposit to savings account is forbidden");
        }

        // Check transfer limit
        if(toAccount.getTransferLimit().compareTo(request.getTransferAmount()) < 0){
            throw new TransferAmountExceedLimitException("Deposit amount exceeds transfer limit");
        }

        // Check account's daily limit
        if(toAccount.getDailyLimit().compareTo(getAccountChangeDay(toAccount.getAccountId()).add(request.getTransferAmount())) < 0){
            throw new DailyLimitExceededException("Account's daily limit exceeded");
        }
    }

    public BigDecimal getAccountChangeDay(UUID accountId) {
        return new BigDecimal(transactionRepository.findAfterDateAccountChange(accountId, getWithinDate()));
    }

    private static LocalDateTime getWithinDate(){
        return LocalDateTime.now().minusDays(1);
    }
}
