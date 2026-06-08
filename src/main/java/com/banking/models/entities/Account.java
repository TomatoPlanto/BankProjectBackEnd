package com.banking.models.entities;

import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id", updatable = false, nullable = false)
    private UUID accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(name = "transfer_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal transferLimit;

    /*
     * Floor — balance can never drop below this.
     * Checked before every debit in TransactionService.
     */
    @Column(name = "absolute_minimum", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal absoluteMinimum = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /*
     * ATM PIN — never exposed in AccountResponseDTO.
     * 0 = unset sentinel. ATM won't accept it (valid range 1000-9999).
     */
    @Column(name = "pin", nullable = false, length = 4)
    @Builder.Default
    private int pin = 0;

    public boolean canDebit(BigDecimal amount) {
        return balance.subtract(amount).compareTo(absoluteMinimum) >= 0;
    }
}