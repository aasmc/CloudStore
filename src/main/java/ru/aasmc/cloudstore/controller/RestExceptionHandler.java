package ru.aasmc.cloudstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.aasmc.cloudstore.exceptions.ItemNotFoundException;
import ru.aasmc.cloudstore.exceptions.ValidationException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class RestExceptionHandler {

    Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        logger.info(e.getMessage());
        if (e instanceof ValidationException) {
            ValidationException ve = (ValidationException) e;
            return new ResponseEntity<>(ve.getMessage(), ve.getStatus());
        } else if (e instanceof ItemNotFoundException) {
            ItemNotFoundException ie = (ItemNotFoundException) e;
            return new ResponseEntity<>(ie.getMessage(), ie.getStatus());
        } else if (e instanceof ConstraintViolationException) {
            return new ResponseEntity<>("Validation Failed", HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
