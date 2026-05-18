package com.banking.repositories;

import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(UUID transactionId);
}
