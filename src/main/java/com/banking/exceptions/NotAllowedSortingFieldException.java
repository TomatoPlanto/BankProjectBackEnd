package com.banking.exceptions;

public class NotAllowedSortingFieldException extends RuntimeException {
    public NotAllowedSortingFieldException(String message) {
        super(message);
    }
}
