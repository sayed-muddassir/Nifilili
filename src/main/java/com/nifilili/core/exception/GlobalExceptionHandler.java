package com.nifilili.core.exception;

import com.nifilili.core.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleSecurityException(AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Registration conflict - email: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Registration conflict - username: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidBusinessState(InvalidBusinessStateException ex, HttpServletRequest request) {
        log.warn("Invalid business state: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidJobState(InvalidJobStateException ex, HttpServletRequest request) {
        log.warn("Invalid job state: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidOrderState(InvalidOrderStateException ex, HttpServletRequest request) {
        log.warn("Invalid order state: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidQuoteState(InvalidQuoteStateException ex, HttpServletRequest request) {
        log.warn("Invalid quote state: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidKycState(InvalidKycStateException ex, HttpServletRequest request) {
        log.warn("Invalid KYC state: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleSqlException(SQLException ex, HttpServletRequest request) {
        log.error("SQL error: {}", ex.getMessage());
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // TODO : Replace with custom exceptions for specific cases (e.g. duplicate application, invalid job status transition, etc.)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Invalid argument error: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // TODO : Replace with custom exceptions for specific cases (e.g. invalid state transition, application withdrawal not allowed, etc.)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(IllegalStateException ex, HttpServletRequest request) {
        log.error("Invalid state error: {}", ex.getMessage());
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleAccountLocked(AccountLockedException ex, HttpServletRequest request) {
        log.warn("Account locked: {}", ex.getMessage());
        return buildError(HttpStatus.LOCKED, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleEmailNotVerified(EmailNotVerifiedException ex, HttpServletRequest request) {
        log.warn("Email not verified: {}", ex.getMessage());
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleInvalidPassword(InvalidPasswordException ex, HttpServletRequest request) {
        log.warn("Invalid password: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleOtpRateLimit(OtpRateLimitException ex, HttpServletRequest request) {
        log.warn("OTP rate limit exceeded: {}", ex.getMessage());
        return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handlePhoneAlreadyExists(PhoneAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Registration conflict - phone: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleUnsupportedAuthMethod(UnsupportedAuthMethodException ex, HttpServletRequest request) {
        log.warn("Unsupported auth method: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for '{}': {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.FORBIDDEN, "You do not have permission to access this resource", request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        log.warn("Method not allowed for '{}': {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleMissingParameter(MissingServletRequestParameterException ex,
                                                           HttpServletRequest request) {
        log.warn("Missing request parameter for '{}': {}", request.getRequestURI(), ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String message = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        log.warn("Type mismatch for '{}': {}", request.getRequestURI(), message);
        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for '{}'", request.getRequestURI());
        return buildError(HttpStatus.NOT_FOUND, "URL not found", request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found for '{}'", request.getRequestURI());
        return buildError(HttpStatus.NOT_FOUND, "URL not found", request);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorDto> handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error for '{}': {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorDto> buildError(HttpStatus status, String message, HttpServletRequest request) {
        return new ResponseEntity<>(new ErrorDto(
                OffsetDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ), status);
    }
}
