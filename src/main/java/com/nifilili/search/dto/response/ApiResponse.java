// FILE: com.nifilili.search.dto.response.ApiResponse
package com.nifilili.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

/**
 * Generic API response wrapper providing a consistent response envelope.
 * <p>
 * All search API endpoints return responses wrapped in this structure,
 * ensuring a uniform contract for API consumers.
 * </p>
 *
 * @param <T> the type of the response payload
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {

    /** Indicates whether the request was processed successfully. */
    @Schema(description = "Whether the request was successful", example = "true")
    private boolean success;

    /** The response payload. */
    @Schema(description = "Response data payload")
    private T data;

    /** A human-readable message providing additional context. */
    @Schema(description = "Human-readable response message", example = "Search completed successfully")
    private String message;

    /** The server timestamp when the response was generated. */
    @Schema(description = "Server timestamp when the response was generated", example = "2026-05-21T08:00:00Z")
    private Instant timestamp;

    /**
     * Creates a successful API response with the given data and message.
     *
     * @param data    the response payload
     * @param message a human-readable success message
     * @param <T>     the type of the response payload
     * @return a new {@link ApiResponse} indicating success
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates an error API response with the given message.
     *
     * @param message a human-readable error message
     * @param <T>     the type of the response payload
     * @return a new {@link ApiResponse} indicating failure
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
