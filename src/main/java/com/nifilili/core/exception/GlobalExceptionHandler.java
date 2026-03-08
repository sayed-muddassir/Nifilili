package com.nifilili.core.exception;

import com.nifilili.core.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleSecurityException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Registration conflict - email: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.CONFLICT.value(), ex.getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        log.warn("Registration conflict - username: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.CONFLICT.value(), ex.getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), message),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.NOT_FOUND.value(), ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidBusinessState(InvalidBusinessStateException ex) {
        log.warn("Invalid business state: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidJobState(InvalidJobStateException ex) {
        log.warn("Invalid job state: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidOrderState(InvalidOrderStateException ex) {
        log.warn("Invalid order state: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidQuoteState(InvalidQuoteStateException ex) {
        log.warn("Invalid quote state: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleSqlException(SQLException ex) {
        log.error("SQL error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // TODO : Replace with custom exceptions for specific cases (e.g. duplicate application, invalid job status transition, etc.)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    // TODO : Replace with custom exceptions for specific cases (e.g. invalid state transition, application withdrawal not allowed, etc.)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalStateException ex) {
        log.error("Invalid state error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
