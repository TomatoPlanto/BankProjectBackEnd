package com.banking.controllers;

import com.banking.models.TransactionModel;
import com.banking.services.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transactions")
public class TransactionController {

    // @Autowired
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("")
    public List<TransactionModel> getTransactions() {
        return this.transactionService.getTransactions();
    }

    @PostMapping("")
    public TransactionModel createTransaction(@RequestBody TransactionModel transaction) {
        return this.transactionService.createTransaction(transaction);
    }

    @DeleteMapping("/delete")
    public void deleteTransaction(@RequestBody TransactionModel transaction) {
        this.transactionService.deleteTransaction(transaction);
    }
}
