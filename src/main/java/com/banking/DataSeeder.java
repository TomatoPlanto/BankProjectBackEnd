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

        // ---- employees (2) ----
        seedEmployee("employee@bank.com", "Bank",  "Employee", "111111111", "0611111111", now.minusYears(3));
        seedEmployee("sarah@bank.com",    "Sarah", "Vance",    "111111112", "0611111112", now.minusYears(2));

        // ---- original active customers (checking + savings) ----
        Account john = seedCustomer("john@email.com", "John", "Doe",     "123456789", "0612345678", UserStatus.ACTIVE, "2500.00", "5000.00",  "1234", now.minusMonths(14));
        Account jane = seedCustomer("jane@email.com", "Jane", "Smith",   "987654321", "0687654321", UserStatus.ACTIVE, "1500.00", "3000.00",  "5678", now.minusMonths(9));
        Account emma = seedCustomer("emma@email.com", "Emma", "Johnson", "222222222", "0622222222", UserStatus.ACTIVE, "4200.00", "12000.00", "2468", now.minusMonths(5));
        Account liam = seedCustomer("liam@email.com", "Liam", "Brown",   "333333333", "0633333333", UserStatus.ACTIVE, "780.50",  "250.00",   "1357", now.minusMonths(2));

        // ---- original closed + pending ----
        seedCustomer("olivia@email.com",  "Olivia",  "Davis",  "444444444", "0644444444", UserStatus.CLOSED,  "0.00", "0.00", "9999", now.minusYears(2));
        seedCustomer("pending@email.com", "Pending", "User",   "555555555", "0655555555", UserStatus.PENDING, null,   null,   null,   now.minusDays(2));
        seedCustomer("noah@email.com",    "Noah",    "Wilson", "666666666", "0666666666", UserStatus.PENDING, null,   null,   null,   now.minusDays(1));

        // ---- new active customers (checking + savings) ----
        Account sophia = seedCustomer("sophia@email.com", "Sophia", "van Dijk",  "100000001", "0701000001", UserStatus.ACTIVE, "3200.00", "8000.00",  "1111", now.minusMonths(20));
        Account lucas  = seedCustomer("lucas@email.com",  "Lucas",  "de Vries",  "100000002", "0701000002", UserStatus.ACTIVE, "900.00",  "1500.00",  "2222", now.minusMonths(18));
        Account mia    = seedCustomer("mia@email.com",    "Mia",    "Bakker",    "100000003", "0701000003", UserStatus.ACTIVE, "6000.00", "20000.00", "3333", now.minusMonths(16));
        Account daan   = seedCustomer("daan@email.com",   "Daan",   "Jansen",    "100000004", "0701000004", UserStatus.ACTIVE, "450.75",  "1200.00",  "4444", now.minusMonths(13));
        Account tess   = seedCustomer("tess@email.com",   "Tess",   "Visser",    "100000005", "0701000005", UserStatus.ACTIVE, "2750.00", "6500.00",  "5555", now.minusMonths(11));
        Account sem    = seedCustomer("sem@email.com",    "Sem",    "Smit",      "100000006", "0701000006", UserStatus.ACTIVE, "1800.00", "4000.00",  "6666", now.minusMonths(8));
        Account julia  = seedCustomer("julia@email.com",  "Julia",  "Meijer",    "100000007", "0701000007", UserStatus.ACTIVE, "5200.00", "15000.00", "7777", now.minusMonths(7));
        Account finn   = seedCustomer("finn@email.com",   "Finn",   "Mulder",    "100000008", "0701000008", UserStatus.ACTIVE, "320.00",  "800.00",   "8888", now.minusMonths(6));
        Account lotte  = seedCustomer("lotte@email.com",  "Lotte",  "de Boer",   "100000009", "0701000009", UserStatus.ACTIVE, "4100.00", "9500.00",  "1212", now.minusMonths(4));
        Account bram   = seedCustomer("bram@email.com",   "Bram",   "Bos",       "100000010", "0701000010", UserStatus.ACTIVE, "670.00",  "2200.00",  "3434", now.minusMonths(3));
        Account eva    = seedCustomer("eva@email.com",    "Eva",    "Peters",    "100000011", "0701000011", UserStatus.ACTIVE, "2300.00", "5600.00",  "5656", now.minusMonths(1));

        // ---- new closed customers (for the CLOSED tab) ----
        seedCustomer("thomas@email.com", "Thomas", "Hendriks", "100000012", "0701000012", UserStatus.CLOSED, "0.00", "0.00", "7878", now.minusYears(1));
        seedCustomer("anna@email.com",   "Anna",   "Dekker",   "100000013", "0701000013", UserStatus.CLOSED, "0.00", "0.00", "9090", now.minusMonths(15));

        // ---- new pending sign-ups (no accounts yet) ----
        seedCustomer("ruben@email.com", "Ruben", "Brouwer", "100000014", "0701000014", UserStatus.PENDING, null, null, null, now.minusDays(3));
        seedCustomer("nina@email.com",  "Nina",  "Vos",     "100000015", "0701000015", UserStatus.PENDING, null, null, null, now.minusHours(6));

        // transactions
        seedTransactions(now, john, jane, emma, liam);
        seedMoreTransactions(now, sophia, lucas, mia, daan, tess, julia, bram, eva);
    }

    private void seedEmployee(String email, String first, String last, String bsn, String phone, LocalDateTime createdAt) {
        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("employee123"))
                .firstName(first).lastName(last)
                .bsn(bsn).phoneNumber(phone)
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

    // extra activity across the newer active accounts so lists/filters aren't all "the original four"
    private void seedMoreTransactions(LocalDateTime now, Account sophia, Account lucas, Account mia,
                                      Account daan, Account tess, Account julia, Account bram, Account eva) {
        // transfers
        save(sophia, mia,   "420.00", "Joint savings",     now.minusDays(1));
        save(mia,    julia, "1200.00","Car deposit",       now.minusDays(3));
        save(lucas,  daan,  "65.00",  "Concert split",     now.minusDays(4));
        save(tess,   eva,   "180.50", "Weekend trip",      now.minusDays(6));
        save(julia,  sophia,"250.00", "Returned loan",     now.minusDays(9));
        save(eva,    bram,  "75.00",  "Dinner",            now.minusDays(12));
        save(bram,   lucas, "40.00",  "Taxi",              now.minusDays(14));
        save(daan,   tess,  "95.25",  "Groceries",         now.minusDays(17));
        save(mia,    eva,   "310.00", "Furniture share",   now.minusDays(22));
        save(julia,  tess,  "500.00", "Rent share",        now.minusDays(29));
        save(sophia, lucas, "60.00",  "Lunch",             now.minusDays(36));
        save(eva,    julia, "140.00", "Tickets",           now.minusDays(44));

        // ATM deposits
        save(null, sophia, "600.00", "ATM deposit",        now.minusDays(5));
        save(null, mia,    "1500.00","ATM deposit",        now.minusDays(11));
        save(null, julia,  "800.00", "ATM deposit",        now.minusDays(19));

        // ATM withdrawals
        save(sophia, null, "150.00", "ATM withdrawal",     now.minusDays(2));
        save(daan,   null, "60.00",  "ATM withdrawal",     now.minusDays(8));
        save(tess,   null, "200.00", "ATM withdrawal",     now.minusDays(21));
        save(bram,   null, "30.00",  "ATM withdrawal",     now.minusDays(38));
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