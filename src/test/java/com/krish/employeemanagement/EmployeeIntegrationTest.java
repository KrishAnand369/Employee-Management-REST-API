package com.krish.employeemanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.UpdateEmployeeRequest;
import com.krish.employeemanagement.entity.EmployeeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("End-to-end CRUD flow: Create, Read, Filter, Update, Delete")
    void fullEmployeeLifecycleIntegrationTest() throws Exception {
        // 1. Create an Employee
        CreateEmployeeRequest createRequest = new CreateEmployeeRequest(
                "John",
                "Doe",
                "john.doe@techcorp.com",
                "Engineering",
                "Backend Engineer",
                BigDecimal.valueOf(105000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 5, 20)
        );

        String createResponse = mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("john.doe@techcorp.com"))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsString();

        Number employeeId = objectMapper.readTree(createResponse).get("id").numberValue();

        // 2. Read by ID
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.department").value("Engineering"));

        // 3. Search and Filter
        mockMvc.perform(get("/api/v1/employees")
                        .param("search", "john")
                        .param("department", "Engineering")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("john.doe@techcorp.com"));

        // 4. Update Employee
        UpdateEmployeeRequest updateRequest = new UpdateEmployeeRequest(
                "John",
                "Doe",
                "john.doe@techcorp.com",
                "Engineering",
                "Senior Backend Engineer",
                BigDecimal.valueOf(125000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 5, 20)
        );

        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.salary").value(125000.00));

        // 5. Delete Employee
        mockMvc.perform(delete("/api/v1/employees/" + employeeId))
                .andExpect(status().isNoContent());

        // 6. Verify 404 after Deletion
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isNotFound());
    }
}
