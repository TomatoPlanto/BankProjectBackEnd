package com.banking.services;

import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.mappers.AccountMapper;
import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountLimitsRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountStatus;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.Interface.IAccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found: " + request.getUserId()));

        Account account = Account.builder()
                .user(user)
                .iban(generateIban())
                .accountType(request.getAccountType())
                .dailyLimit(request.getDailyLimit())
                .transferLimit(request.getTransferLimit())
                .absoluteMinimum(request.getAbsoluteMinimum())
                .balance(BigDecimal.ZERO)
                .pin(0)
                .build();

        return AccountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public AccountResponseDTO getAccountById(UUID accountId) {
        return AccountMapper.toDTO(
                accountRepository.findById(accountId)
                        .orElseThrow(() -> new AccountNotFoundException(
                                "Account not found: " + accountId))
        );
    }

    @Override
    public AccountResponseDTO getAccountByIban(String iban) {
        return AccountMapper.toDTO(
                accountRepository.findByIban(iban)
                        .orElseThrow(() -> new AccountNotFoundException(
                                "Account not found for IBAN: " + iban))
        );
    }

    @Override
    public List<AccountResponseDTO> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserUserId(userId)
                .stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO updateLimits(UUID accountId, UpdateAccountLimitsRequestDTO request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId));

        account.setDailyLimit(request.getDailyLimit());
        account.setTransferLimit(request.getTransferLimit());
        account.setAbsoluteMinimum(request.getAbsoluteMinimum());

        return AccountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public AccountResponseDTO closeAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId));

        account.setStatus(AccountStatus.CLOSED);
        return AccountMapper.toDTO(accountRepository.save(account));
    }

    @Override
    public AccountResponseDTO reactivateAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId));

        account.setStatus(AccountStatus.ACTIVE);
        return AccountMapper.toDTO(accountRepository.save(account));
    }

    public String generateIban() {
        String bankCode = "INHO";
        String iban;
        do {
            String accountNum = String.format("%010d",
                    (long)(Math.random() * 9_000_000_000L) + 1_000_000_000L);
            String rawIban = "NL00" + bankCode + "0" + accountNum;
            int checkDigits = calculateCheckDigits(rawIban);
            iban = "NL" + String.format("%02d", checkDigits) + bankCode + "0" + accountNum;
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    public int calculateCheckDigits(String rawIban) {
        String rearranged = rawIban.substring(4) + rawIban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            numeric.append(Character.isLetter(c) ? (c - 'A' + 10) : c);
        }
        int remainder = new java.math.BigInteger(numeric.toString())
                .mod(java.math.BigInteger.valueOf(97)).intValue();
        return 98 - remainder;
    }
}