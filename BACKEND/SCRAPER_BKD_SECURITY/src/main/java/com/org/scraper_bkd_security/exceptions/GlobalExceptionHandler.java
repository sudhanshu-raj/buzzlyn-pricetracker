package com.org.scraper_bkd_security.exceptions;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<String> handleUserRegistrationException(final UserRegistrationException ex){
        return ResponseEntity.status(ex.getStatus())
                .body("Error: "+ex.getMessage());
    }

    @ExceptionHandler(UserRegistrationWarning.class)
    public ResponseEntity<String> handleUserRegistrationWarning(final UserRegistrationWarning ex){
        return ResponseEntity.status(ex.getStatus())
                .body("Error: "+ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName+ " error", errorMessage);
        });
        errors.put("success","false");
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
