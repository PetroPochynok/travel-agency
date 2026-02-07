package com.epam.finaltask.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler({
            VoucherNotFoundException.class,
            UserNotFoundException.class,
            UsernameAlreadyExistsException.class,
            VoucherOrderException.class,
            InvalidUuidException.class
    })
    public ResponseEntity<ApiError> handleBusinessExceptions(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiError> handleTransaction(TransactionException ex, Locale locale) {
        String localizedMessage = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(localizedMessage, LocalDateTime.now()));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, Locale locale) {
        String localizedMessage = messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("email: " + localizedMessage, LocalDateTime.now()));
    }

    @ExceptionHandler(InvalidDatesException.class)
    public ResponseEntity<ApiError> handleInvalidDates(InvalidDatesException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError("Access Denied", LocalDateTime.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(ex.getMessage(), LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), messageSource.getMessage(error, locale)));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

}
