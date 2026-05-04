package com.banking.controllers;

import com.banking.models.dto.request.UpdateBalanceRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.services.Interface.IAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banking.models.dto.request.AtmLoginRequestDTO;
import com.banking.models.dto.response.LoginResponseDTO;
import com.banking.services.Interface.IAtmService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/atm")
public class AtmController {

    private final IAtmService atmService;
    private final IAccountService accountService;

    public AtmController(IAtmService atmService, IAccountService accountService) {
        this.atmService = atmService;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> atmLogin(@Valid @RequestBody AtmLoginRequestDTO request) {
        LoginResponseDTO response = atmService.atmLogin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{accountId}/withdraw")
    public ResponseEntity<AccountResponseDTO> getWithdrawPage(@PathVariable UUID accountId) {
        AccountResponseDTO response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/withdraw")
    public ResponseEntity<LoginResponseDTO> withdraw(@Valid @RequestBody UpdateBalanceRequestDTO request) { // need to fix the @ on this one
        LoginResponseDTO response = atmService.updateBalance(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{accountId}/deposit")
    public ResponseEntity<AccountResponseDTO> getDepositPage(@PathVariable UUID accountId) {
        AccountResponseDTO response = accountService.getAccountById(accountId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/deposit")
    public ResponseEntity<LoginResponseDTO> deposit(@Valid @RequestBody UpdateBalanceRequestDTO request) { // need to fix the @ on this one
        LoginResponseDTO response = atmService.updateBalance(request);
        return ResponseEntity.ok(response);
    }
}
