package com.banking.services;

import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.mappers.AccountMapper;
import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountStatus;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.Interface.IAccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
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
                .balance(BigDecimal.ZERO)
                .build();

        Account saved = accountRepository.save(account);
        return AccountMapper.toDTO(saved);
    }

    @Override
    public AccountResponseDTO getAccountById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
        return AccountMapper.toDTO(account);
    }

    @Override
    public AccountResponseDTO getAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Account not with iban was not found"));
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
    public Page<AccountResponseDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(AccountMapper::toDTO);
    }

    /*
     * Partial update: only non-null fields are applied, so one endpoint
     * covers "update a limit", "update all limits", "close", "re-open",
     * or any combination. Employee-only (enforced in the controller).
     */
    @Override
    public AccountResponseDTO updateAccount(UUID accountId, UpdateAccountRequestDTO request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));

        if (request.getDailyLimit() != null)      account.setDailyLimit(request.getDailyLimit());
        if (request.getTransferLimit() != null)   account.setTransferLimit(request.getTransferLimit());
        if (request.getAbsoluteMinimum() != null) account.setAbsoluteMinimum(request.getAbsoluteMinimum());
        if (request.getStatus() != null)          account.setStatus(request.getStatus());

        return AccountMapper.toDTO(accountRepository.save(account));
    }

    public String generateIban() {
        String bankCode = "INHO";
        String accountNumber = String.format("%010d",
                (long) (Math.random() * 9_000_000_000L) + 1_000_000_000L);
        String rawIban = "NL00" + bankCode + "0" + accountNumber;
        int checkDigits = calculateCheckDigits(rawIban);
        return "NL" + String.format("%02d", checkDigits) + bankCode + "0" + accountNumber;
    }

    public int calculateCheckDigits(String rawIban) {
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