package com.banking.exceptions;

public class TransactionFormatException extends RuntimeException {
    public TransactionFormatException(String message) {
        super(message);
    }
}
