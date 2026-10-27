package com.krish.employeemanagement.dto;

import com.krish.employeemanagement.entity.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload to register a new employee")
public record CreateEmployeeRequest(

        @Schema(description = "Employee first name", example = "Jane")
        @NotBlank(message = "First name must not be blank")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Schema(description = "Employee last name", example = "Doe")
        @NotBlank(message = "Last name must not be blank")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Schema(description = "Work email address (must be unique)", example = "jane.doe@company.com")
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @Schema(description = "Department name", example = "Engineering")
        @NotBlank(message = "Department must not be blank")
        @Size(min = 2, max = 50, message = "Department must be between 2 and 50 characters")
        String department,

        @Schema(description = "Job title / position", example = "Senior Software Engineer")
        @NotBlank(message = "Position must not be blank")
        @Size(min = 2, max = 80, message = "Position must be between 2 and 80 characters")
        String position,

        @Schema(description = "Annual salary", example = "115000.00")
        @NotNull(message = "Salary must be specified")
        @Positive(message = "Salary must be greater than zero")
        BigDecimal salary,

        @Schema(description = "Employment status", example = "ACTIVE", defaultValue = "ACTIVE")
        EmployeeStatus status,

        @Schema(description = "Date of hiring (defaults to today if omitted)", example = "2024-03-15")
        @PastOrPresent(message = "Hire date cannot be in the future")
        LocalDate hireDate
) {
}
