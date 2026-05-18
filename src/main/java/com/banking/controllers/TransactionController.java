package com.banking.controllers;

import com.banking.models.dto.request.RegisterRequestDTO;
import com.banking.models.dto.request.TransferBetweenAccountsRequestDTO;
import com.banking.models.dto.response.AccountResponseDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.services.Interface.ITransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService){
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getAccountById(@PathVariable UUID transactionId) {
        TransactionResponseDTO response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transferBetweenAccounts(@Valid @RequestBody TransferBetweenAccountsRequestDTO request) {
        TransactionResponseDTO response = transactionService.transferBetweenAccounts(request);
        return ResponseEntity.ok(response);
    }
}
