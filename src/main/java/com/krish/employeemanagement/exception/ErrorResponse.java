package com.krish.employeemanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard API error response payload")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status name", example = "Bad Request")
        String error,

        @Schema(description = "Detailed error description", example = "Validation failed for one or more fields")
        String message,

        @Schema(description = "URI path of the request", example = "/api/v1/employees")
        String path,

        @Schema(description = "UTC timestamp of the error occurrence", example = "2026-10-27T15:30:00Z")
        Instant timestamp,

        @Schema(description = "List of specific field validation errors, if applicable")
        List<ValidationError> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now(), null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(status, error, message, path, Instant.now(), validationErrors);
    }

    @Schema(description = "Individual field validation error detail")
    public record ValidationError(
            @Schema(description = "Name of the invalid property", example = "email")
            String field,

            @Schema(description = "The rejected invalid value", example = "invalid-email")
            Object rejectedValue,

            @Schema(description = "Validation message", example = "Email format is invalid")
            String message
    ) {
    }
}
