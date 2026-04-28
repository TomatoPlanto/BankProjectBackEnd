package com.banking.repositories;

import com.banking.models.entities.Account;
import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIban(String iban);

    boolean existsByIban(String iban);

    List<Account> findByUserUserId(UUID userId);

    List<Account> findByUserUserIdAndAccountType(UUID userId, AccountType accountType);

    List<Account> findByStatus(AccountStatus status);

    List<Account> findByUserUserIdAndStatus(UUID userId, AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.status = :status")
    List<Account> findActiveAccountsByUser(
            @Param("userId") UUID userId,
            @Param("status") AccountStatus status
    );

    @Query("SELECT a FROM Account a WHERE a.iban = :iban AND a.status = 'ACTIVE'")
    Optional<Account> findActiveByIban(@Param("iban") String iban);
}