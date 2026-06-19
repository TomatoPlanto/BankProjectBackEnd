package com.banking.controllers;

import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.services.Interface.IAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        AccountResponseDTO response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountSecurity.isAccountOwner(#accountId, authentication.name)")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID accountId) {
        AccountResponseDTO response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/iban/{iban}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable String iban) {
        AccountResponseDTO response = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountSecurity.isSelf(#userId, authentication.name)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountResponseDTO> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<Page<AccountResponseDTO>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    // update limits and/or status in one call (employee only)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponseDTO> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountRequestDTO request) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }
}