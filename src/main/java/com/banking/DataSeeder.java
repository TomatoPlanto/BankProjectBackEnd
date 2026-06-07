package com.banking;

import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import com.banking.models.enums.*;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.AccountService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountService accountService;

    public DataSeeder(UserRepository userRepository,
                      AccountRepository accountRepository,
                      TransactionRepository transactionRepository,
                      BCryptPasswordEncoder passwordEncoder,
                      AccountService accountService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedEmployee();
        Account johnChecking = seedJohn();
        Account janeChecking = seedJane();
        seedTransactions(johnChecking, janeChecking);
        seedPendingUser();
    }

    private void seedEmployee() {
        if (userRepository.existsByEmail("employee@bank.com")) return;
        userRepository.save(User.builder()
                .email("employee@bank.com")
                .passwordHash(passwordEncoder.encode("employee123"))
                .firstName("Bank")
                .lastName("Employee")
                .bsn("111111111")
                .phoneNumber("0611111111")
                .role(UserRole.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build());
    }

    private Account seedJohn() {
        if (userRepository.existsByEmail("john@email.com")) return null;

        User john = userRepository.save(User.builder()
                .email("john@email.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .firstName("John")
                .lastName("Doe")
                .bsn("123456789")
                .phoneNumber("0612345678")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        Account checking = accountRepository.save(Account.builder()
                .user(john)
                .iban(accountService.generateIban())
                .accountType(AccountType.CHECKING)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .balance(new BigDecimal("2500.00"))
                .status(AccountStatus.ACTIVE)
                .pin("1234")
                .build());

        accountRepository.save(Account.builder()
                .user(john)
                .iban(accountService.generateIban())
                .accountType(AccountType.SAVINGS)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .balance(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .pin("1234")
                .build());

        return checking;
    }

    private Account seedJane() {
        if (userRepository.existsByEmail("jane@email.com")) return null;

        User jane = userRepository.save(User.builder()
                .email("jane@email.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .firstName("Jane")
                .lastName("Smith")
                .bsn("987654321")
                .phoneNumber("0687654321")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build());

        Account checking = accountRepository.save(Account.builder()
                .user(jane)
                .iban(accountService.generateIban())
                .accountType(AccountType.CHECKING)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .balance(new BigDecimal("1500.00"))
                .status(AccountStatus.ACTIVE)
                .pin("5678")
                .build());

        accountRepository.save(Account.builder()
                .user(jane)
                .iban(accountService.generateIban())
                .accountType(AccountType.SAVINGS)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .balance(new BigDecimal("3000.00"))
                .status(AccountStatus.ACTIVE)
                .pin("5678")
                .build());

        return checking;
    }

    /*
     * Seed realistic history so the transaction page has data on first run.
     * null fromAccount = deposit, null toAccount = withdrawal.
     */
    private void seedTransactions(Account johnChecking, Account janeChecking) {
        if (johnChecking == null || janeChecking == null) return;

        transactionRepository.save(txn(johnChecking, janeChecking, "350.00", "Transfer to Jane"));
        transactionRepository.save(txn(janeChecking, johnChecking, "396.99", "Transfer to John"));
        transactionRepository.save(txn(johnChecking, janeChecking, "134.55", "Transfer to Jane"));
        transactionRepository.save(txn(null,         janeChecking, "134.55", "ATM deposit"));
        transactionRepository.save(txn(janeChecking, null,         "134.55", "ATM withdrawal"));
    }

    private Transaction txn(Account from, Account to, String amount, String desc) {
        return Transaction.builder()
                .fromAccount(from)
                .toAccount(to)
                .amount(new BigDecimal(amount))
                .description(desc)
                .type(TransactionType.CUSTOMER_TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void seedPendingUser() {
        if (userRepository.existsByEmail("pending@email.com")) return;
        userRepository.save(User.builder()
                .email("pending@email.com")
                .passwordHash(passwordEncoder.encode("secret123"))
                .firstName("Pending")
                .lastName("User")
                .bsn("555555555")
                .phoneNumber("0655555555")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING)
                .build());
    }
}