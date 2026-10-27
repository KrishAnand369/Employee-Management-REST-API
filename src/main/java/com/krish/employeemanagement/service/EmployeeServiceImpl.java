package com.krish.employeemanagement.service;

import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.EmployeeFilterCriteria;
import com.krish.employeemanagement.dto.EmployeeResponse;
import com.krish.employeemanagement.dto.UpdateEmployeeRequest;
import com.krish.employeemanagement.entity.Employee;
import com.krish.employeemanagement.exception.DuplicateEmailException;
import com.krish.employeemanagement.exception.ResourceNotFoundException;
import com.krish.employeemanagement.repository.EmployeeRepository;
import com.krish.employeemanagement.repository.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Attempting to create employee with email: '{}', department: '{}'", request.email(), request.department());

        if (employeeRepository.existsByEmail(request.email())) {
            log.warn("Employee creation failed: email '{}' already in use", request.email());
            throw new DuplicateEmailException(request.email());
        }

        Employee employee = new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.department(),
                request.position(),
                request.salary(),
                request.status(),
                request.hireDate()
        );

        Employee saved = employeeRepository.save(employee);
        log.info("Successfully created employee [ID: {}, email: '{}', department: '{}']",
                saved.getId(), saved.getEmail(), saved.getDepartment());
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        log.debug("Fetching employee by ID: {}", id);
        return employeeRepository.findById(id)
                .map(employee -> {
                    log.debug("Found employee [ID: {}, email: '{}']", employee.getId(), employee.getEmail());
                    return EmployeeResponse.from(employee);
                })
                .orElseThrow(() -> {
                    log.warn("Employee lookup failed: ID '{}' not found", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });
    }

    @Override
    public Page<EmployeeResponse> getAllEmployees(EmployeeFilterCriteria criteria, Pageable pageable) {
        log.info("Querying employees: search='{}', department='{}', status='{}', page={}, size={}, sort={}",
                criteria != null ? criteria.search() : null,
                criteria != null ? criteria.department() : null,
                criteria != null ? criteria.status() : null,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Specification<Employee> spec = EmployeeSpecification.withCriteria(criteria);
        Page<Employee> page = employeeRepository.findAll(spec, pageable);

        log.info("Found {} employees matching criteria (total elements: {}, total pages: {})",
                page.getNumberOfElements(), page.getTotalElements(), page.getTotalPages());

        return page.map(EmployeeResponse::from);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Attempting to update employee [ID: {}]", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employee update failed: ID '{}' not found", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            log.warn("Employee update failed: email '{}' is already registered to another employee", request.email());
            throw new DuplicateEmailException(request.email());
        }

        employee.changeEmail(request.email());
        employee.updateDetails(
                request.firstName(),
                request.lastName(),
                request.department(),
                request.position(),
                request.salary(),
                request.status(),
                request.hireDate()
        );

        Employee updated = employeeRepository.save(employee);
        log.info("Successfully updated employee [ID: {}, email: '{}', version: {}]",
                updated.getId(), updated.getEmail(), updated.getVersion());
        return EmployeeResponse.from(updated);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Attempting to delete employee [ID: {}]", id);

        if (!employeeRepository.existsById(id)) {
            log.warn("Employee deletion failed: ID '{}' not found", id);
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        log.info("Successfully deleted employee [ID: {}]", id);
    }
}
