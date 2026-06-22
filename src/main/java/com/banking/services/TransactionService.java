package com.banking.services;

import com.banking.exceptions.*;
import com.banking.mappers.TransactionMapper;
import com.banking.models.dto.request.GetTransactionsRequestDTO;
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
import com.banking.repositories.specifications.TransactionSpecs;
import com.banking.services.Interface.ITransactionService;
import jakarta.persistence.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository, TransactionPolicy transactionPolicy) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;

        this.transactionPolicy = transactionPolicy;
    }

    @Override
    @Transactional
    public TransactionResponseDTO getTransactionById(UUID transactionId){
        var transaction = transactionRepository.findByTransactionId(transactionId);
        if(transaction.isEmpty()) throw new TransactionNotFoundException("Transaction not found with id: " + transactionId);

        return TransactionMapper.toDTO(transaction.get());
    }

    @Override
    @Transactional
    public TransactionResponseDTO transfer(TransferRequestDTO request){
        // Get accounts
        Account from = getAccountForTransaction(request.getFromAccountId(), "Failed to get sender's account");
        Account to = getAccountForTransaction(request.getToAccountId(), "Failed to get receiver's account");

        // Get transfer type
        User initiator = getLoggedInUser();
        TransactionType transactionType = initiator.getRole() == UserRole.EMPLOYEE ? TransactionType.EMPLOYEE_TRANSFER : TransactionType.CUSTOMER_TRANSFER;

        // Make transaction
        if(from == null){
            if(to == null) throw new TransactionFormatException("Both sender's and receiver's account id is null");

            return depositToAccount(request, to, transactionType);
        }
        else{
            if(to == null){
                return withdrawFromAccount(request, from, transactionType);
            }
            else{
                return transferBetweenAccounts(request, from, to, transactionType);
            }
        }
    }

    private Account getAccountForTransaction(UUID accountId, String failMessage){
        if(accountId == null) return null;

        var accountWrap = accountRepository.findByAccountId(accountId);
        if(accountWrap.isEmpty()) throw new AccountNotFoundException(failMessage);

        return accountWrap.get();
    }

    private TransactionResponseDTO transferBetweenAccounts(TransferRequestDTO request, Account fromAccount, Account toAccount, TransactionType transactionType){
        // Enforce transaction policy
        transactionPolicy.enforceTransferBetweenAccounts(request, fromAccount, toAccount);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description(request.getDescription())
                .type(transactionType)
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

    private TransactionResponseDTO withdrawFromAccount(TransferRequestDTO request, Account fromAccount, TransactionType transactionType){
        // Enforce transaction policy
        transactionPolicy.enforceWithdrawFromAccounts(request, fromAccount);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(null)
                .amount(request.getTransferAmount())
                .description("Withdrawal")
                .type(transactionType)
                .createdAt(LocalDateTime.now())
                .build();

        // Add transaction
        transactionRepository.save(trans);

        // Remove money from the account
        accountRepository.setAccountBalance(fromAccount.getAccountId(), fromAccount.getBalance().subtract(request.getTransferAmount()));

        return TransactionMapper.toDTO(trans);
    }

    private TransactionResponseDTO depositToAccount(TransferRequestDTO request, Account toAccount, TransactionType transactionType){
        // Enforce transaction policy
        transactionPolicy.enforceDepositToAccounts(request, toAccount);

        // Create transaction
        Transaction trans = Transaction.builder()
                .fromAccount(null)
                .toAccount(toAccount)
                .amount(request.getTransferAmount())
                .description("Deposit")
                .type(transactionType)
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
    public Page<TransactionResponseDTO> getTransactions(GetTransactionsRequestDTO request){
        if(request.getAccountId() != null) return getAccountTransactions(request);

        return getAllTransactions(request);
    }

    @Transactional
    public Page<TransactionResponseDTO> getAllTransactions(GetTransactionsRequestDTO request){
        // Get transactions
        var trans = transactionRepository.findAll(getRequestSpecs(request) ,getRequestPage(request));

        // Map transactions to DTO
        return trans.map(TransactionMapper::toDTO);
    }

    @Transactional
    public Page<TransactionResponseDTO> getAccountTransactions(GetTransactionsRequestDTO request){
        // Get account and check if it exists
        var account = accountRepository.findByAccountId(request.getAccountId());
        if(account.isEmpty()) throw new AccountNotFoundException("Account not found");

        // Get transactions
        var trans = transactionRepository.findAccountTransactions(request.getAccountId(), getRequestSpecs(request), getRequestPage(request));

        // Map transactions to DTO
        return trans.map(TransactionMapper::toDTO);
    }

    private PageRequest getRequestPage(GetTransactionsRequestDTO request){
        if(!request.getSorting().isEmpty()){
            // Check if sorting field is allowed
            if(!ALLOWED_SORTINGS.contains(request.getSorting())) throw new NotAllowedSortingFieldException("Sorting by unknown field");

            // Use sorting and order
            return PageRequest.of(request.getPageNumber(), request.getTransactionsPerPage(), request.isSortingOrder() ? Sort.by(request.getSorting()).ascending() : Sort.by(request.getSorting()).descending());
        }

        return PageRequest.of(request.getPageNumber(), request.getTransactionsPerPage());
    }

    private Specification<Transaction> getRequestSpecs(GetTransactionsRequestDTO request){
        ArrayList<Specification<Transaction>> specs = new ArrayList<Specification<Transaction>>();

        // Iban specs
        if(request.getFilterToAccountIban() != null) specs.add(TransactionSpecs.toAccountIbanEquals(request.getFilterToAccountIban()));
        if(request.getFilterFromAccountIban() != null) specs.add(TransactionSpecs.fromAccountIbanEquals(request.getFilterFromAccountIban()));

        // Amount specs
        if(request.getFilterEqualAmount() != null) {
            specs.add(TransactionSpecs.amountEquals(request.getFilterEqualAmount()));
        }
        else {
            if(request.getFilterMinAmount() != null) specs.add(TransactionSpecs.amountGreaterThan(request.getFilterMinAmount()));
            if(request.getFilterMaxAmount() != null) specs.add(TransactionSpecs.amountLessThan(request.getFilterMaxAmount()));
        }

        return Specification.allOf(specs);
    }

    @Override
    public CountResponseDTO getAccountTransactionsCount(UUID accountId){
        int count = transactionRepository.countAllAccountTransactions(accountId);

        CountResponseDTO res = new CountResponseDTO();
        res.setCount(count);

        return res;
    }

    private User getLoggedInUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        var wrap = userRepository.findByEmail(currentEmail);
        if(wrap.isEmpty()) throw new RuntimeException("Failed to get transaction initiator for transfer");

        return wrap.get();
    }
}
