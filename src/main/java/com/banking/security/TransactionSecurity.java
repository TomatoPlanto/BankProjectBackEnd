package com.banking.security;

import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.UserRole;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.AtmRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

// used by @PreAuthorize so a customer can only reach their own transactions
@Component("transactionSecurity")
public class TransactionSecurity {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AtmRepository atmRepository;

    public TransactionSecurity(TransactionRepository transactionRepository, AccountRepository accountRepository, UserRepository userRepository, AtmRepository atmRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.atmRepository = atmRepository;
    }

    public boolean isTransactionCoOwner(UUID transactionId, String email) {
        if (transactionId == null || email == null) return false;

        var trans = transactionRepository.findById(transactionId);
        if(trans.isEmpty()) return false;

        var from = trans.get().getFromAccount();
        var to = trans.get().getToAccount();

        return from.getUser().getEmail() == email || to.getUser().getEmail() == email;
    }

    public boolean isTransferAllowed(TransferRequestDTO request){
        // Get user
        var initiator = getLoggedInUser();

        if(initiator.getRole() == UserRole.EMPLOYEE) return true;

        if(request.getFromAccountId() == null){
            if(request.getToAccountId() == null) return false;

            var toAccountWrap = accountRepository.findByAccountId(request.getToAccountId());
            if(toAccountWrap.isEmpty()) return false;
            var toAccount = toAccountWrap.get();

            return toAccount.getUser().getUserId().equals(initiator.getUserId());
        }

        var fromAccountWrap = accountRepository.findByAccountId(request.getFromAccountId());
        if(fromAccountWrap.isEmpty()) return false;
        var fromAccount = fromAccountWrap.get();

        return fromAccount.getUser().getUserId().equals(initiator.getUserId());
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
}
