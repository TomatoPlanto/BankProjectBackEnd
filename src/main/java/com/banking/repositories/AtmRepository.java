package com.banking.repositories;

import java.util.Optional;
import java.util.UUID;

import com.banking.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banking.models.entities.Account;

@Repository
public interface AtmRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByIban(String iban);
}
