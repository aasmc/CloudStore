package ru.aasmc.cloudstore.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {
    private HttpStatus status;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ValidationException(HttpStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
