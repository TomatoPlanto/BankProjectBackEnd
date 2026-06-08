package com.banking.controllers;

import com.banking.models.dto.request.CreateAccountRequestDTO;
import com.banking.models.dto.request.UpdateAccountLimitsRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.services.Interface.IAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        AccountResponseDTO response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable UUID accountId) {
        AccountResponseDTO response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable String iban) {
        AccountResponseDTO response = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(@PathVariable UUID userId) {
        List<AccountResponseDTO> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAllAccounts() {
        List<AccountResponseDTO> response = accountService.getAllAccounts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/limits")
    public ResponseEntity<AccountResponseDTO> updateLimits(
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateAccountLimitsRequestDTO request) {
        AccountResponseDTO response = accountService.updateLimits(accountId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/close")
    public ResponseEntity<AccountResponseDTO> closeAccount(@PathVariable UUID accountId) {
        AccountResponseDTO response = accountService.closeAccount(accountId);
        return ResponseEntity.ok(response);
    }
}