package com.banking.services;

import com.banking.models.TransactionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private List<TransactionModel> transactions = new ArrayList<TransactionModel>();

    public TransactionService() {
        TransactionModel transaction = new TransactionModel();
        transaction.setFromAccount("12345");
        transaction.setToAccount("56789");
        transaction.setAmount(233.01);
        transactions.add(transaction);
    }

    public List<TransactionModel> getTransactions() {
        return transactions;
    }

    public TransactionModel createTransaction(TransactionModel transaction) {
        this.transactions.add(transaction);
        return transaction;
    }

    // no work because no id. funny try tho
    public void deleteTransaction(@RequestBody TransactionModel transaction) {
        transactions.remove(transaction);
    }
}
