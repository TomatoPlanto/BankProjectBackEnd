package com.banking.exceptions;

public class TransactionFromSavingAccountException extends RuntimeException {
    public TransactionFromSavingAccountException(String message) {
        super(message);
    }
}
