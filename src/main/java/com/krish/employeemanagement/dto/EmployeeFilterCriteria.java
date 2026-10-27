package com.krish.employeemanagement.dto;

import com.krish.employeemanagement.entity.EmployeeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Query parameters for searching and filtering employees")
public record EmployeeFilterCriteria(

        @Schema(description = "Search term to match across first name, last name, or email (case-insensitive)", example = "jane")
        String search,

        @Schema(description = "Filter by exact department name (case-insensitive)", example = "Engineering")
        String department,

        @Schema(description = "Filter by employee status", example = "ACTIVE")
        EmployeeStatus status
) {
}
