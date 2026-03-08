package com.nifilili.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "ErrorResponse",
        description = "Standard API error payload returned when a request cannot be processed successfully."
)
public class ErrorDto {

    @Schema(
            description = "Timestamp when the error response was generated in ISO-8601 offset date-time format.",
            example = "2026-03-08T10:15:30+05:30"
    )
    private String timestamp;

    @Schema(description = "HTTP status code for the error response.", example = "400")
    private Integer status;

    @Schema(description = "HTTP reason phrase associated with the status code.", example = "Bad Request")
    private String error;

    @Schema(
            description = "Human-readable explanation of the failure. Validation errors may include one or more field-level messages.",
            example = "password: Password must be at least 8 characters"
    )
    private String message;

    @Schema(description = "Request path that triggered the error.", example = "/api/auth/register")
    private String path;
}
