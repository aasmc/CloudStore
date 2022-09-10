package ru.aasmc.cloudstore.exceptions;

import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends RuntimeException {

    private HttpStatus status;

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemNotFoundException(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
