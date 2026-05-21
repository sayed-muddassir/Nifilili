// FILE: com.nifilili.core.exception.SearchExecutionException
package com.nifilili.core.exception;

/**
 * Runtime exception thrown when a search query fails to execute.
 * <p>
 * This exception is caught by the global {@code @ControllerAdvice} exception handler
 * and translated into an appropriate HTTP error response.
 * </p>
 */
public class SearchExecutionException extends RuntimeException {

    /**
     * Creates a new SearchExecutionException with the specified message.
     *
     * @param message a human-readable description of the search failure
     */
    public SearchExecutionException(String message) {
        super(message);
    }

    /**
     * Creates a new SearchExecutionException with the specified message and cause.
     *
     * @param message a human-readable description of the search failure
     * @param cause   the underlying exception that caused the search failure
     */
    public SearchExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
