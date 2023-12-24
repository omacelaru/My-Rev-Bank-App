package com.fmi.rev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidAmountException extends InvalidTransactionException {
    public InvalidAmountException() {
        super("Invalid amount");
    }
}
