package com.fmi.rev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidTransactionException extends BadRequestException {
    public InvalidTransactionException() {
        super("Invalid transaction");
    }

    public InvalidTransactionException(String message) {
        super(message);
    }
}
