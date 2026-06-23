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
        if (userRepository.existsByEmail("employee@bank.com")) return; // already seeded

        LocalDateTime now = LocalDateTime.now();

        seedEmployee(now.minusYears(3));

        // active customers - these get a checking + savings account
        Account john = seedCustomer("john@email.com", "John", "Doe",     "123456789", "0612345678", UserStatus.ACTIVE, "2500.00", "5000.00",  "1234", now.minusMonths(14));
        Account jane = seedCustomer("jane@email.com", "Jane", "Smith",   "987654321", "0687654321", UserStatus.ACTIVE, "1500.00", "3000.00",  "5678", now.minusMonths(9));
        Account emma = seedCustomer("emma@email.com", "Emma", "Johnson", "222222222", "0622222222", UserStatus.ACTIVE, "4200.00", "12000.00", "2468", now.minusMonths(5));
        Account liam = seedCustomer("liam@email.com", "Liam", "Brown",   "333333333", "0633333333", UserStatus.ACTIVE, "780.50",  "250.00",   "1357", now.minusMonths(2));

        // a closed customer (for the CLOSED tab) and two pending sign-ups (no accounts yet)
        seedCustomer("olivia@email.com",  "Olivia",  "Davis",  "444444444", "0644444444", UserStatus.CLOSED,  "0.00", "0.00", "9999", now.minusYears(2));
        seedCustomer("pending@email.com", "Pending", "User",   "555555555", "0655555555", UserStatus.PENDING, null,   null,   null,   now.minusDays(2));
        seedCustomer("noah@email.com",    "Noah",    "Wilson", "666666666", "0666666666", UserStatus.PENDING, null,   null,   null,   now.minusDays(1));

        seedTransactions(now, john, jane, emma, liam);
    }

    private void seedEmployee(LocalDateTime createdAt) {
        userRepository.save(User.builder()
                .email("employee@bank.com")
                .passwordHash(passwordEncoder.encode("employee123"))
                .firstName("Bank").lastName("Employee")
                .bsn("111111111").phoneNumber("0611111111")
                .role(UserRole.EMPLOYEE).status(UserStatus.ACTIVE)
                .createdAt(createdAt)
                .build());
    }

    // makes a customer; active/closed ones also get a checking + savings account
    private Account seedCustomer(String email, String first, String last, String bsn, String phone,
                                 UserStatus status, String checkingBalance, String savingsBalance,
                                 String pin, LocalDateTime createdAt) {
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("secret123"))
                .firstName(first).lastName(last).bsn(bsn).phoneNumber(phone)
                .role(UserRole.CUSTOMER).status(status)
                .createdAt(createdAt)
                .build());

        // pending customers have no accounts until an employee approves them
        if (status == UserStatus.PENDING) return null;

        Account checking = accountRepository.save(account(user, AccountType.CHECKING, checkingBalance, pin, status));
        accountRepository.save(account(user, AccountType.SAVINGS, savingsBalance, pin, status));
        return checking;
    }

    private Account account(User user, AccountType type, String balance, String pin, UserStatus userStatus) {
        AccountStatus accountStatus = userStatus == UserStatus.CLOSED ? AccountStatus.CLOSED : AccountStatus.ACTIVE;
        return Account.builder()
                .user(user)
                .iban(accountService.generateIban())
                .accountType(type)
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .balance(new BigDecimal(balance))
                .status(accountStatus)
                .pin(pin)
                .build();
    }

    // null fromAccount = ATM deposit, null toAccount = ATM withdrawal, both set = transfer
    private void seedTransactions(LocalDateTime now, Account john, Account jane, Account emma, Account liam) {
        // customer to customer transfers
        save(john, jane, "350.00", "Rent share",         now.minusDays(1));
        save(jane, john, "120.00", "Dinner last week",   now.minusDays(2));
        save(emma, john, "75.50",  "Concert tickets",    now.minusDays(3));
        save(john, emma, "200.00", "Holiday fund",        now.minusDays(5));
        save(liam, jane, "45.00",  "Coffee tab",          now.minusDays(6));
        save(jane, emma, "310.25", "Furniture",           now.minusDays(8));
        save(emma, liam, "90.00",  "Gym membership",      now.minusDays(9));
        save(john, jane, "350.00", "Rent share",          now.minusDays(11));
        save(jane, liam, "60.00",  "Birthday gift",       now.minusDays(13));
        save(emma, john, "150.00", "Loan repayment",      now.minusDays(15));
        save(liam, emma, "25.00",  "Lunch",               now.minusDays(18));
        save(john, emma, "480.00", "Shared trip",         now.minusDays(21));
        save(jane, john, "95.99",  "Groceries",           now.minusDays(24));
        save(emma, jane, "215.00", "Deposit return",      now.minusDays(28));
        save(john, liam, "130.00", "Tools",               now.minusDays(33));
        save(jane, emma, "300.00", "Rent share",          now.minusDays(40));
        save(liam, john, "55.00",  "Movie night",         now.minusDays(47));
        save(emma, john, "350.00", "Rent share",          now.minusDays(55));

        // ATM deposits
        save(null, john, "500.00", "ATM deposit",         now.minusDays(2));
        save(null, jane, "250.00", "ATM deposit",         now.minusDays(7));
        save(null, emma, "1000.00","ATM deposit",         now.minusDays(12));
        save(null, liam, "175.00", "ATM deposit",         now.minusDays(20));
        save(null, jane, "300.00", "ATM deposit",         now.minusDays(35));

        // ATM withdrawals
        save(john, null, "100.00", "ATM withdrawal",      now.minusDays(1));
        save(jane, null, "80.00",  "ATM withdrawal",      now.minusDays(4));
        save(emma, null, "250.00", "ATM withdrawal",      now.minusDays(10));
        save(liam, null, "40.00",  "ATM withdrawal",      now.minusDays(16));
        save(john, null, "200.00", "ATM withdrawal",      now.minusDays(30));
        save(emma, null, "120.00", "ATM withdrawal",      now.minusDays(50));
    }

    private void save(Account from, Account to, String amount, String desc, LocalDateTime when) {
        transactionRepository.save(Transaction.builder()
                .fromAccount(from)
                .toAccount(to)
                .amount(new BigDecimal(amount))
                .description(desc)
                .type(TransactionType.CUSTOMER_TRANSFER)
                .createdAt(when)
                .build());
    }
}