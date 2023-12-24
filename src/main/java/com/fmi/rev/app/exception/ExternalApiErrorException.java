package com.fmi.rev.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class ExternalApiErrorException extends InternalServerErrorException {
    public ExternalApiErrorException() {
        super("External API error");
    }

    public ExternalApiErrorException(String message) {
        super(message);
    }
}
