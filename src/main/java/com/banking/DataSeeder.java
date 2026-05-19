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

        if (!userRepository.existsByEmail("employee@bank.com")) {
            User employee = User.builder()
                    .email("employee@bank.com")
                    .passwordHash(passwordEncoder.encode("employee123"))
                    .firstName("Bank")
                    .lastName("Employee")
                    .bsn("111111111")
                    .phoneNumber("0611111111")
                    .role(UserRole.EMPLOYEE)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(employee);
        }

        if (!userRepository.existsByEmail("john@email.com")) {
            User customer = User.builder()
                    .email("john@email.com")
                    .passwordHash(passwordEncoder.encode("secret123"))
                    .firstName("John")
                    .lastName("Doe")
                    .bsn("123456789")
                    .phoneNumber("0612345678")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            User savedCustomer = userRepository.save(customer);

            Account johnCheckAccount = Account.builder()
                    .user(savedCustomer)
                    .iban(accountService.generateIban())
                    .accountType(AccountType.CHECKING)
                    .dailyLimit(new BigDecimal("1000.00"))
                    .todayChange(BigDecimal.ZERO)
                    .transferLimit(new BigDecimal("500.00"))
                    .absoluteMinimum(BigDecimal.ZERO)
                    .balance(new BigDecimal("2500.00"))
                    .status(AccountStatus.ACTIVE)
                    .build();

            Account johnSaveAccount = Account.builder()
                    .user(savedCustomer)
                    .iban(accountService.generateIban())
                    .accountType(AccountType.SAVINGS)
                    .dailyLimit(new BigDecimal("1000.00"))
                    .todayChange(BigDecimal.ZERO)
                    .transferLimit(new BigDecimal("500.00"))
                    .absoluteMinimum(BigDecimal.ZERO)
                    .balance(new BigDecimal("5000.00"))
                    .status(AccountStatus.ACTIVE)
                    .build();

            accountRepository.save(johnSaveAccount);
            accountRepository.save(johnCheckAccount);

            transactionRepository.save(Transaction.builder()
                    .fromAccount(johnCheckAccount)
                    .toAccount(johnSaveAccount)
                    .amount(new BigDecimal("300.13"))
                    .description("Transfer to savings account")
                    .type(TransactionType.CUSOMER_TRANSFER)
                    .createdAt(LocalDateTime.now())
                    .build());

            transactionRepository.save(Transaction.builder()
                    .fromAccount(johnSaveAccount)
                    .toAccount(johnCheckAccount)
                    .amount(new BigDecimal("299.99"))
                    .description("Transfer from savings account")
                    .type(TransactionType.CUSOMER_TRANSFER)
                    .createdAt(LocalDateTime.now())
                    .build());

            transactionRepository.save(Transaction.builder()
                    .fromAccount(johnCheckAccount)
                    .toAccount(johnSaveAccount)
                    .amount(new BigDecimal("413.55"))
                    .description("Transfer to savings account")
                    .type(TransactionType.CUSOMER_TRANSFER)
                    .createdAt(LocalDateTime.now())
                    .build());

            if (!userRepository.existsByEmail("jane@email.com")) {
                User customer2 = User.builder()
                        .email("jane@email.com")
                        .passwordHash(passwordEncoder.encode("secret123"))
                        .firstName("Jane")
                        .lastName("Smith")
                        .bsn("987654321")
                        .phoneNumber("0687654321")
                        .role(UserRole.CUSTOMER)
                        .status(UserStatus.ACTIVE)
                        .build();
                User savedCustomer2 = userRepository.save(customer2);

                accountRepository.save(Account.builder()
                        .user(savedCustomer2)
                        .iban(accountService.generateIban())
                        .accountType(AccountType.SAVINGS)
                        .dailyLimit(new BigDecimal("1000.00"))
                        .todayChange(BigDecimal.ZERO)
                        .transferLimit(new BigDecimal("500.00"))
                        .absoluteMinimum(BigDecimal.ZERO)
                        .balance(new BigDecimal("3000.00"))
                        .status(AccountStatus.ACTIVE)
                        .build());

                Account janeCheckAccount = Account.builder()
                        .user(savedCustomer2)
                        .iban(accountService.generateIban())
                        .accountType(AccountType.CHECKING)
                        .dailyLimit(new BigDecimal("1000.00"))
                        .todayChange(BigDecimal.ZERO)
                        .transferLimit(new BigDecimal("500.00"))
                        .absoluteMinimum(BigDecimal.ZERO)
                        .balance(new BigDecimal("1500.00"))
                        .status(AccountStatus.ACTIVE)
                        .build();

                accountRepository.save(janeCheckAccount);

                transactionRepository.save(Transaction.builder()
                        .fromAccount(johnCheckAccount)
                        .toAccount(janeCheckAccount)
                        .amount(new BigDecimal("350.00"))
                        .description("Transfer to jane checking account")
                        .type(TransactionType.CUSOMER_TRANSFER)
                        .createdAt(LocalDateTime.now())
                        .build());

                transactionRepository.save(Transaction.builder()
                        .fromAccount(janeCheckAccount)
                        .toAccount(johnCheckAccount)
                        .amount(new BigDecimal("396.99"))
                        .description("Transfer to john checking account")
                        .type(TransactionType.CUSOMER_TRANSFER)
                        .createdAt(LocalDateTime.now())
                        .build());

                transactionRepository.save(Transaction.builder()
                        .fromAccount(johnCheckAccount)
                        .toAccount(janeCheckAccount)
                        .amount(new BigDecimal("134.55"))
                        .description("Transfer to jane checking account")
                        .type(TransactionType.CUSOMER_TRANSFER)
                        .createdAt(LocalDateTime.now())
                        .build());

                transactionRepository.save(Transaction.builder()
                        .fromAccount(null)
                        .toAccount(janeCheckAccount)
                        .amount(new BigDecimal("134.55"))
                        .description("Deposit to jane checking account")
                        .type(TransactionType.CUSOMER_TRANSFER)
                        .createdAt(LocalDateTime.now())
                        .build());

                transactionRepository.save(Transaction.builder()
                        .fromAccount(janeCheckAccount)
                        .toAccount(null)
                        .amount(new BigDecimal("134.55"))
                        .description("Withdrawal from jane checking account")
                        .type(TransactionType.CUSOMER_TRANSFER)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
        }

        if (!userRepository.existsByEmail("pending@email.com")) {
            User pending = User.builder()
                    .email("pending@email.com")
                    .passwordHash(passwordEncoder.encode("secret123"))
                    .firstName("Pending")
                    .lastName("User")
                    .bsn("555555555")
                    .phoneNumber("0655555555")
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.PENDING)
                    .build();
            userRepository.save(pending);
        }
    }
}