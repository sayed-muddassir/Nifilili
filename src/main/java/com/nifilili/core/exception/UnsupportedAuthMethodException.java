package com.nifilili.core.exception;

/**
 * Thrown when an unsupported authentication method is requested.
 */
public class UnsupportedAuthMethodException extends RuntimeException {

    public UnsupportedAuthMethodException(String message) {
        super(message);
    }
}
