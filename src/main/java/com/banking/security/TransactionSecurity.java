package com.banking.security;

import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.enums.UserRole;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

// used by @PreAuthorize so a customer can only reach their own transactions
@Component("transactionSecurity")
public class TransactionSecurity {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionSecurity(TransactionRepository transactionRepository, AccountRepository accountRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isTransactionCoOwner(UUID transactionId, String email) {
        if (transactionId == null || email == null) return false;

        var trans = transactionRepository.findById(transactionId);
        if(trans.isEmpty()) return false;

        var from = trans.get().getFromAccount();
        var to = trans.get().getToAccount();

        return from.getUser().getEmail() == email || to.getUser().getEmail() == email;
    }

    public boolean isTransferAllowed(TransferRequestDTO request, String email){
        // Get user
        var initiatorWrap = userRepository.findByEmail(email);
        if(initiatorWrap.isEmpty()) return false;
        var initiator = initiatorWrap.get();

        if(initiator.getRole() == UserRole.EMPLOYEE)return true;

        if(request.getFromAccountId() == null){
            if(request.getToAccountId() == null) return false;

            var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
            if(toAccountWrap.isEmpty()) return false;
            var toAccount = toAccountWrap.get();

            return toAccount.getUser().getUserId() == initiator.getUserId();
        }

        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) return false;
        var fromAccount = fromAccountWrap.get();

        return fromAccount.getUser().getUserId() == initiator.getUserId();
    }
}
