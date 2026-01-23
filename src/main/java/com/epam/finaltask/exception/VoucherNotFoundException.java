package com.epam.finaltask.exception;

public class VoucherNotFoundException extends RuntimeException {

    public VoucherNotFoundException(String message) {
        super(message);
    }
}