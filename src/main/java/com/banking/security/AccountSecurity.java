package com.banking.security;

import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

// used by @PreAuthorize so a customer can only reach their own account
@Component("accountSecurity")
public class AccountSecurity {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountSecurity(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public boolean isAccountOwner(UUID accountId, String email) {
        if (accountId == null || email == null) return false;
        return accountRepository.findById(accountId)
                .map(account -> email.equals(account.getUser().getEmail()))
                .orElse(false);
    }

    public boolean isSelf(UUID userId, String email) {
        if (userId == null || email == null) return false;
        return userRepository.findByEmail(email)
                .map(user -> userId.equals(user.getUserId()))
                .orElse(false);
    }
}