package com.krish.employeemanagement.service;

import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.EmployeeFilterCriteria;
import com.krish.employeemanagement.dto.EmployeeResponse;
import com.krish.employeemanagement.dto.UpdateEmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    Page<EmployeeResponse> getAllEmployees(EmployeeFilterCriteria criteria, Pageable pageable);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void deleteEmployee(Long id);
}
