package com.banking.exceptions;

public class TransferAmountExceedLimitException extends RuntimeException {
    public TransferAmountExceedLimitException(String message) {
        super(message);
    }
}
