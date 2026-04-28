package com.banking.exceptions;

public class DuplicateBsnException extends RuntimeException {
    public DuplicateBsnException(String message) {
        super(message);
    }
}