package com.krish.employeemanagement.dto;

import com.krish.employeemanagement.entity.Employee;
import com.krish.employeemanagement.entity.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Standard representation of an Employee")
public record EmployeeResponse(

        @Schema(description = "Unique employee identifier", example = "1")
        Long id,

        @Schema(description = "Employee first name", example = "Jane")
        String firstName,

        @Schema(description = "Employee last name", example = "Doe")
        String lastName,

        @Schema(description = "Employee email", example = "jane.doe@company.com")
        String email,

        @Schema(description = "Department name", example = "Engineering")
        String department,

        @Schema(description = "Position / job title", example = "Senior Software Engineer")
        String position,

        @Schema(description = "Annual salary", example = "115000.00")
        BigDecimal salary,

        @Schema(description = "Current employment status", example = "ACTIVE")
        EmployeeStatus status,

        @Schema(description = "Date of hiring", example = "2024-03-15")
        LocalDate hireDate,

        @Schema(description = "Optimistic locking version counter", example = "0")
        Long version,

        @Schema(description = "Timestamp when employee was created", example = "2024-03-15T09:30:00Z")
        Instant createdAt,

        @Schema(description = "Timestamp when employee was last updated", example = "2024-03-15T09:30:00Z")
        Instant updatedAt
) {
    public static EmployeeResponse from(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getPosition(),
                employee.getSalary(),
                employee.getStatus(),
                employee.getHireDate(),
                employee.getVersion(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}
