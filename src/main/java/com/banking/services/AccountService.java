package com.banking.services;

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
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + request.getUserId()));

        Account account = Account.builder()
                .user(user)
                .iban(generateIban())
                .accountType(request.getAccountType())
                .dailyLimit(request.getDailyLimit())
                .transferLimit(request.getTransferLimit())
                .absoluteMinimum(request.getAbsoluteMinimum())
                .build();

        Account saved = accountRepository.save(account);
        return AccountMapper.toDTO(saved);
    }

    @Override
    public AccountResponseDTO getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        return AccountMapper.toDTO(account);
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
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        account.setDailyLimit(request.getDailyLimit());
        account.setTransferLimit(request.getTransferLimit());
        account.setAbsoluteMinimum(request.getAbsoluteMinimum());

        Account saved = accountRepository.save(account);
        return AccountMapper.toDTO(saved);
    }

    @Override
    public AccountResponseDTO closeAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        account.setStatus(AccountStatus.CLOSED);
        Account saved = accountRepository.save(account);
        return AccountMapper.toDTO(saved);
    }

    private String generateIban() {
        String bankCode = "INHO";
        String accountNumber = String.format("%010d",
                (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
        String rawIban = "NL00" + bankCode + "0" + accountNumber;
        int checkDigits = calculateCheckDigits(rawIban);
        return "NL" + String.format("%02d", checkDigits) + bankCode + "0" + accountNumber;
    }

    private int calculateCheckDigits(String rawIban) {
        String rearranged = rawIban.substring(4) + rawIban.substring(0, 4);
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(c - 'A' + 10);
            } else {
                numericIban.append(c);
            }
        }
        java.math.BigInteger ibanNumber = new java.math.BigInteger(numericIban.toString());
        int remainder = ibanNumber.mod(java.math.BigInteger.valueOf(97)).intValue();
        return 98 - remainder;
    }
}