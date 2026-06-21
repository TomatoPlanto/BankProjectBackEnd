package com.banking.controllers;

import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.request.TransferRequestDTO;
import com.banking.models.dto.response.CountResponseDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.entities.User;
import com.banking.services.Interface.ITransactionService;
import com.banking.services.Interface.IUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PreAuthorize("hasRole('EMPLOYEE') or @transactionSecurity.isTransactionCoOwner(#transactionId, authentication.name)")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getAccountById(@PathVariable UUID transactionId) {
        TransactionResponseDTO response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @transactionSecurity.isTransferAllowed(#request, authentication.name)")
    @PostMapping()
    public ResponseEntity<TransactionResponseDTO> transferBetweenAccounts(@Valid @RequestBody TransferRequestDTO request) {
        TransactionResponseDTO response = transactionService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountSecurity.isAccountOwner(#accountId, authentication.name)")
    @GetMapping()
    public ResponseEntity<Page<TransactionResponseDTO>> getAllAccountTransaction(@RequestParam UUID accountId,
            @RequestParam int pageNumber, @RequestParam int transactionsPerPage, @RequestParam String sorting, @RequestParam boolean sortingOrder) {

        var request = GetAccountTransactionsRequestDTO.builder()
                .accountId(accountId)
                .pageNumber(pageNumber)
                .transactionsPerPage(transactionsPerPage)
                .sorting(sorting)
                .sortingOrder(sortingOrder)
                .build();

        var response = transactionService.getAccountTransactions(request);
        return ResponseEntity.ok(response);
    }
}
