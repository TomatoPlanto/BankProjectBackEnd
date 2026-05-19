package com.banking.repositories;

import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(UUID transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.toAccount.accountId = ?1 OR t.fromAccount.accountId  = ?1")
    List<Transaction> findAllAccountTransactions(UUID accountId);
}
