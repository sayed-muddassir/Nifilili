package com.nifilili.core.exception;

/**
 * Thrown when a phone number is already associated with an existing account.
 */
public class PhoneAlreadyExistsException extends RuntimeException {

    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
}
