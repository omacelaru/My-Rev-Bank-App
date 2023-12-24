package com.fmi.rev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class TransactionNotFoundException extends NotFoundException {
    public TransactionNotFoundException() {
        super("Transaction not found");
    }
}
