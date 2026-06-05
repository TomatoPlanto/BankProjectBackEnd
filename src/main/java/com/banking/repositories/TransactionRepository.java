package com.banking.repositories;

import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByTransactionId(UUID transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.toAccount.accountId = ?1 OR t.fromAccount.accountId = ?1 ORDER BY t.createdAt")
    List<Transaction> findAccountTransactions(UUID accountId, Pageable pageable);

    @Query("SELECT COUNT(*) FROM Transaction t WHERE t.toAccount.accountId  = ?1 OR t.fromAccount.accountId = ?1")
    int countAllAccountTransactions(UUID accountId);

    @Query("SELECT COUNT(*) FROM Transaction t WHERE t.createdAt >= ?2 AND (t.toAccount.accountId = ?1 OR t.fromAccount.accountId = ?1)")
    String findAfterDateAccountChange(UUID accountId, LocalDateTime afterDate);
}
