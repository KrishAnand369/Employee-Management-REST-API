package com.krish.employeemanagement.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of the employee")
public enum EmployeeStatus {

    @Schema(description = "Employee is active")
    ACTIVE,

    @Schema(description = "Employee is inactive")
    INACTIVE,

    @Schema(description = "Employee is resigned")
    RESIGNED,

    @Schema(description = "Employee is on probation")
    PROBATION,

    @Schema(description = "Employee is terminated")
    TERMINATED
}
