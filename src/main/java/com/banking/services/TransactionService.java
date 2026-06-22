package com.banking.services;

import com.banking.exceptions.*;
import com.banking.mappers.TransactionMapper;
import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.dto.response.CountResponseDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import com.banking.models.enums.TransactionType;
import com.banking.models.enums.UserRole;
import com.banking.policy.TransactionPolicy;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import com.banking.repositories.AtmRepository;
import com.banking.services.Interface.ITransactionService;
import jakarta.persistence.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {
    public final static List<String> ALLOWED_SORTINGS = Arrays.asList("amount", "type", "description", "createdAt");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionPolicy transactionPolicy;
    private final AtmRepository atmRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository, TransactionPolicy transactionPolicy, AtmRepository atmRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionPolicy = transactionPolicy;
        this.atmRepository = atmRepository;
    }

    @Override
    public TransactionResponseDTO getTransactionById(UUID transactionId){
        var transaction = transactionRepository.findByTransactionId(transactionId);
        if(transaction.isEmpty()) throw new TransactionNotFoundException("Transaction not found with id: " + transactionId);

        return TransactionMapper.toDTO(transaction.get());
    }

    @Override
    @Transactional
    public TransactionResponseDTO transfer(TransferRequestDTO request){
        Account from = getAccountForTransaction(request.getFromAccountId(), "Failed to get sender's account");
        Account to = getAccountForTransaction(request.getToAccountId(), "Failed to get receiver's account");

        User initiator = getLoggedInUser();

        if(from == null){
            if(to == null) throw new TransactionFormatException("Both sender's and receiver's account id is null");

            return depositToAccount(request, to, initiator);
        }
        else{
            if(to == null){
                return withdrawFromAccount(request, from, initiator);
            }
            else{
                return transferBetweenAccounts(request, from, to, initiator);
            }
        }
    }

    private Account getAccountForTransaction(UUID accountId, String failMessage){
        if(accountId == null) return null;

        var accountWrap = accountRepository.findByAccountId(accountId);
        if(accountWrap.isEmpty()) throw new AccountNotFoundException(failMessage);

        return accountWrap.get();
    }

    private User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = auth.getName();

        // Check if this is an ATM session (principal is an IBAN, not an email)
        boolean isAtm = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ATM_ACCOUNT"));

        if (isAtm) {
            // Principal is the IBAN — look up the account owner
            Account account = atmRepository.findByIbanIgnoreCase(principal)
                    .orElseThrow(() -> new RuntimeException("ATM session account not found"));
            return account.getUser();
        }

        // Regular user session — principal is email
        return userRepository.findByEmail(principal)
                .orElseThrow(() -> new RuntimeException("Failed to get transaction initiator for transfer"));
    }

    private TransactionResponseDTO transferBetweenAccounts(TransferRequestDTO request, Account fromAccount, Account toAccount, User initiator){
        // Enforce transaction policy
        transactionPolicy.enforceTransferBetweenAccounts(request, fromAccount, toAccount, initiator);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description(request.getDescription())
                .type(initiator.getRole() == UserRole.EMPLOYEE ? TransactionType.EMPLOYEE_TRANSFER : TransactionType.CUSTOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Save transaction
        transactionRepository.save(trans);

        // Remove money from fromAccount
        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getTransferAmount()));
        // Add money to toAccount
        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    private TransactionResponseDTO withdrawFromAccount(TransferRequestDTO request, Account fromAccount, User initiator){
        // Enforce transaction policy
        transactionPolicy.enforceWithdrawFromAccounts(request, fromAccount, initiator);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(null)
                .amount(request.getTransferAmount())
                .description("Withdrawal")
                .type(initiator.getRole() == UserRole.EMPLOYEE ? TransactionType.EMPLOYEE_TRANSFER : TransactionType.CUSTOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Add transaction
        transactionRepository.save(trans);

        // Remove money from the account
        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    private TransactionResponseDTO depositToAccount(TransferRequestDTO request, Account toAccount, User initiator){
        // Enforce transaction policy
        transactionPolicy.enforceDepositToAccounts(request, toAccount, initiator);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(null)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description("Deposit")
                .type(initiator.getRole() == UserRole.EMPLOYEE ? TransactionType.EMPLOYEE_TRANSFER : TransactionType.CUSTOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        // Add transaction
        transactionRepository.save(trans);

        // Add money to account
        accountRepository.setAccountBalance(toAccount.getAccountId(), toAccount.getBalance().add(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    @Override
    @Transactional
    public Page<TransactionResponseDTO> getAccountTransactions(GetAccountTransactionsRequestDTO request){
        // Get account and check if it exists
        var account = accountRepository.findByAccountId(request.getAccountId());
        if(account.isEmpty()) throw new AccountNotFoundException("Account not found");

        // Enforce get transactions policy
        transactionPolicy.enforceGetAccountTransactions(account.get(), getLoggedInUser());

        PageRequest pageReq;

        if(!request.getSorting().isEmpty()){
            // Check if sorting field is allowed
            if(!ALLOWED_SORTINGS.contains(request.getSorting())) throw new NotAllowedSortingFieldException("Sorting by unknown field");

            // Use sorting and order
            if(request.isSortingOrder()){
                pageReq = PageRequest.of(request.getPageNumber(), request.getTransactionsPerPage(), Sort.by(request.getSorting()).ascending());
            }
            else {
                pageReq = PageRequest.of(request.getPageNumber(), request.getTransactionsPerPage(), Sort.by(request.getSorting()).descending());
            }
        }
        else{
            pageReq = PageRequest.of(request.getPageNumber(), request.getTransactionsPerPage());
        }

        // Get transactions
        var trans = transactionRepository.findAccountTransactions(request.getAccountId(), pageReq);

        // Map transactions to DTO
        return trans.map(TransactionMapper::toDTO);
    }

    @Override
    public CountResponseDTO getAccountTransactionsCount(UUID accountId){
        int count = transactionRepository.countAllAccountTransactions(accountId);

        CountResponseDTO res = new CountResponseDTO();
        res.setCount(count);

        return res;
    }
}
