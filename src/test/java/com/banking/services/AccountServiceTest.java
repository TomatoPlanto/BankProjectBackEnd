package com.banking.services;

import com.banking.exceptions.AccountNotFoundException;
import com.banking.exceptions.UserNotFoundException;
import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.dto.response.IbanLookupResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountStatus;
import com.banking.models.enums.AccountType;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock UserRepository userRepository;
    @InjectMocks AccountService accountService;

    private User user;
    private Account account;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        user = User.builder()
                .userId(userId).email("john@email.com")
                .firstName("John").lastName("Doe")
                .build();
        account = Account.builder()
                .accountId(accountId).user(user)
                .iban("NL00INHO0123456789")
                .accountType(AccountType.CHECKING)
                .balance(new BigDecimal("100.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .transferLimit(new BigDecimal("500.00"))
                .absoluteMinimum(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void createAccount_savesWithZeroBalanceAndOwnerName() {
        CreateAccountRequestDTO req = new CreateAccountRequestDTO();
        req.setUserId(userId);
        req.setAccountType(AccountType.CHECKING);
        req.setDailyLimit(new BigDecimal("1000.00"));
        req.setTransferLimit(new BigDecimal("500.00"));
        req.setAbsoluteMinimum(BigDecimal.ZERO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDTO dto = accountService.createAccount(req);

        assertEquals(AccountType.CHECKING, dto.getAccountType());
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getBalance()));
        assertEquals("John Doe", dto.getOwnerName());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_userMissing_throws() {
        CreateAccountRequestDTO req = new CreateAccountRequestDTO();
        req.setUserId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> accountService.createAccount(req));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccountById_found_returnsDto() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        AccountResponseDTO dto = accountService.getAccountById(accountId);
        assertEquals(accountId, dto.getAccountId());
        assertEquals("John Doe", dto.getOwnerName());
    }

    @Test
    void getAccountById_missing_throws() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    void getAccountByIban_returnsMinimalLookup() {
        when(accountRepository.findByIban("NL00INHO0123456789")).thenReturn(Optional.of(account));
        IbanLookupResponseDTO dto = accountService.getAccountByIban("NL00INHO0123456789");
        assertEquals(accountId, dto.getAccountId());
        assertEquals("NL00INHO0123456789", dto.getIban());
        assertEquals("John Doe", dto.getOwnerName());
    }

    @Test
    void getAccountByIban_missing_throws() {
        when(accountRepository.findByIban("x")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByIban("x"));
    }

    @Test
    void searchAccountsByOwner_returnsOnlyActiveAccounts() {
        Account closed = Account.builder()
                .accountId(UUID.randomUUID()).user(user)
                .iban("NL99INHO9999999999").status(AccountStatus.CLOSED).build();
        when(accountRepository.findByUserFirstNameContainingIgnoreCaseOrUserLastNameContainingIgnoreCase("john", "john"))
                .thenReturn(List.of(account, closed));

        List<IbanLookupResponseDTO> result = accountService.searchAccountsByOwner("john");

        assertEquals(1, result.size());
        assertEquals("NL00INHO0123456789", result.get(0).getIban());
        assertEquals("John Doe", result.get(0).getOwnerName());
    }

    @Test
    void updateAccount_partial_onlyUpdatesProvidedFields() {
        UpdateAccountRequestDTO req = new UpdateAccountRequestDTO();
        req.setDailyLimit(new BigDecimal("250.00")); // only this one
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDTO dto = accountService.updateAccount(accountId, req);

        assertEquals(0, new BigDecimal("250.00").compareTo(dto.getDailyLimit()));
        assertEquals(0, new BigDecimal("500.00").compareTo(dto.getTransferLimit())); // untouched
        assertEquals(AccountStatus.ACTIVE, dto.getStatus());                          // untouched
    }

    @Test
    void updateAccount_canCloseViaStatus() {
        UpdateAccountRequestDTO req = new UpdateAccountRequestDTO();
        req.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponseDTO dto = accountService.updateAccount(accountId, req);
        assertEquals(AccountStatus.CLOSED, dto.getStatus());
    }

    @Test
    void updateAccount_missing_throws() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> accountService.updateAccount(accountId, new UpdateAccountRequestDTO()));
    }

    @Test
    void getAccountsByUserId_mapsList() {
        when(accountRepository.findByUserUserId(userId)).thenReturn(List.of(account));
        List<AccountResponseDTO> result = accountService.getAccountsByUserId(userId);
        assertEquals(1, result.size());
        assertEquals(accountId, result.get(0).getAccountId());
    }

    @Test
    void generateIban_passesMod97Check() {
        String iban = accountService.generateIban();
        assertTrue(iban.startsWith("NL"));

        // standard IBAN validity: move first 4 chars to the end, letters -> numbers, mod 97 == 1
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(c - 'A' + 10);
            } else {
                numeric.append(c); // append the digit char as-is, not its int value
            }
        }
        assertEquals(1, new BigInteger(numeric.toString()).mod(BigInteger.valueOf(97)).intValue());
    }
}