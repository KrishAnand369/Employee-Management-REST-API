package com.krish.employeemanagement.controller;

import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.EmployeeFilterCriteria;
import com.krish.employeemanagement.dto.EmployeeResponse;
import com.krish.employeemanagement.dto.UpdateEmployeeRequest;
import com.krish.employeemanagement.entity.EmployeeStatus;
import com.krish.employeemanagement.exception.ErrorResponse;
import com.krish.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for employee CRUD, search, pagination, and status filtering")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(
            summary = "Create a new employee",
            description = "Registers a new employee in the system with initial details. " +
                    "Validates unique email, salary positive constraint, and required personal/department information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee successfully created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed for request fields",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate email address encountered",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        EmployeeResponse response = employeeService.createEmployee(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Get employee by ID",
            description = "Retrieves the complete profile and status of an employee matching the specified unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee profile found and returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee with given ID does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Unique ID of the employee", required = true, example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(
            summary = "List employees with search, filtering, and pagination",
            description = "Retrieves a paginated list of employees. Supports multi-term search across name and email, " +
                    "filtering by department and employment status, and customizable sorting."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of employees returned successfully"
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @Parameter(description = "Search term to match against first name, last name, or email")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by department name (e.g. Engineering, HR, Finance)")
            @RequestParam(required = false) String department,

            @Parameter(description = "Filter by employee status (ACTIVE, INACTIVE, PROBATION, TERMINATED)")
            @RequestParam(required = false) EmployeeStatus status,

            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        EmployeeFilterCriteria criteria = new EmployeeFilterCriteria(search, department, status);
        return ResponseEntity.ok(employeeService.getAllEmployees(criteria, pageable));
    }

    @Operation(
            summary = "Update employee details",
            description = "Updates an existing employee's details. Enforces unique email check (ignoring current employee), " +
                    "validates payload, and utilizes optimistic locking to avoid concurrent overwrite hazards."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee successfully updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed for request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee with given ID not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate email address or concurrent update conflict (optimistic lock)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @Parameter(description = "Unique ID of the employee to update", required = true, example = "1")
            @PathVariable Long id,

            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @Operation(
            summary = "Delete an employee",
            description = "Deletes the employee record identified by the given ID from the database."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Employee deleted successfully (no content returned)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee with given ID not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "Unique ID of the employee to delete", required = true, example = "1")
            @PathVariable Long id
    ) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
