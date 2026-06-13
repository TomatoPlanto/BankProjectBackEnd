package com.banking.controllers;

import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.dto.response.CountResponseDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.services.Interface.ITransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PostMapping()
    public ResponseEntity<TransactionResponseDTO> transferBetweenAccounts(@Valid @RequestBody TransferRequestDTO request) {
        TransactionResponseDTO response = transactionService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account")
    public ResponseEntity<Page<TransactionResponseDTO>> getAllAccountTransaction(@Valid @RequestBody GetAccountTransactionsRequestDTO request) {
        var response = transactionService.getAccountTransactions(request);
        return ResponseEntity.ok(response);
    }
}
