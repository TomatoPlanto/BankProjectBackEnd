package com.banking.security;

import com.banking.models.entities.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountDetailsService implements UserDetails {
    private final Account account;

    public AccountDetailsService(Account account) {
        this.account = account;
    }

    @Override
    public String getUsername() { return account.getIban(); }

    @Override
    public String getPassword() { return null; } // not used for JWT generation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_ATM_ACCOUNT"));
    }
}