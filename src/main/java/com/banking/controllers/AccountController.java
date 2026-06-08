package com.banking.controllers;

import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountLimitsRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.services.Interface.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Create account — employee only")
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody CreateAccountRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get account by ID — employee only")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/iban/{iban}")
    @Operation(summary = "Find account by IBAN")
    public ResponseEntity<AccountResponseDTO> getAccountByIban(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getAccountByIban(iban));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all accounts for a user — employee only")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Get all accounts — employee only")
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/{accountId}/limits")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Update account limits — employee only")
    public ResponseEntity<AccountResponseDTO> updateLimits(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountLimitsRequestDTO request) {
        return ResponseEntity.ok(accountService.updateLimits(accountId, request));
    }

    @PutMapping("/{accountId}/close")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Close account — employee only")
    public ResponseEntity<AccountResponseDTO> closeAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.closeAccount(accountId));
    }

    @PutMapping("/{accountId}/reactivate")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Reactivate a closed account — employee only")
    public ResponseEntity<AccountResponseDTO> reactivateAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.reactivateAccount(accountId));
    }
}