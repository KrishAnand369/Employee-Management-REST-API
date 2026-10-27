package com.krish.employeemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.EmployeeResponse;
import com.krish.employeemanagement.entity.EmployeeStatus;
import com.krish.employeemanagement.exception.ResourceNotFoundException;
import com.krish.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("POST /api/v1/employees - Should return 201 Created and Location header on valid request")
    void createEmployee_WhenValid_ShouldReturn201() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(95000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15)
        );

        EmployeeResponse response = new EmployeeResponse(
                1L,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(95000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15),
                0L,
                Instant.now(),
                Instant.now()
        );

        given(employeeService.createEmployee(any(CreateEmployeeRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/employees/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/employees - Should return 400 Bad Request when validation fails")
    void createEmployee_WhenInvalidPayload_ShouldReturn400() throws Exception {
        CreateEmployeeRequest invalidRequest = new CreateEmployeeRequest(
                "", // Blank first name
                "Doe",
                "not-an-email", // Invalid email
                "",
                "",
                BigDecimal.valueOf(-100), // Negative salary
                EmployeeStatus.ACTIVE,
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} - Should return 200 OK when employee exists")
    void getEmployeeById_WhenFound_ShouldReturn200() throws Exception {
        EmployeeResponse response = new EmployeeResponse(
                1L,
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(95000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15),
                0L,
                Instant.now(),
                Instant.now()
        );

        given(employeeService.getEmployeeById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} - Should return 404 Not Found when employee does not exist")
    void getEmployeeById_WhenNotFound_ShouldReturn404() throws Exception {
        given(employeeService.getEmployeeById(999L))
                .willThrow(new ResourceNotFoundException("Employee", "id", 999L));

        mockMvc.perform(get("/api/v1/employees/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: '999'"));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} - Should return 204 No Content")
    void deleteEmployee_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1"))
                .andExpect(status().isNoContent());
    }
}
