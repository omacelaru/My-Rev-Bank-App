package com.fmi.rev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidAccountException extends InvalidTransactionException {
    public InvalidAccountException() {
        super("Invalid account");
    }
}
