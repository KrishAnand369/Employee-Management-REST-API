package com.krish.employeemanagement.service;

import com.krish.employeemanagement.dto.CreateEmployeeRequest;
import com.krish.employeemanagement.dto.EmployeeResponse;
import com.krish.employeemanagement.dto.UpdateEmployeeRequest;
import com.krish.employeemanagement.entity.Employee;
import com.krish.employeemanagement.entity.EmployeeStatus;
import com.krish.employeemanagement.exception.DuplicateEmailException;
import com.krish.employeemanagement.exception.ResourceNotFoundException;
import com.krish.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee(
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "Engineering",
                "Software Engineer",
                BigDecimal.valueOf(95000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15)
        );
        ReflectionTestUtils.setField(sampleEmployee, "id", 1L);
        ReflectionTestUtils.setField(sampleEmployee, "version", 0L);
    }

    @Test
    @DisplayName("Should successfully create an employee when email is unique")
    void createEmployee_WhenEmailIsUnique_ShouldSaveAndReturnResponse() {
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

        given(employeeRepository.existsByEmail(request.email())).willReturn(false);
        given(employeeRepository.save(any(Employee.class))).willReturn(sampleEmployee);

        EmployeeResponse response = employeeService.createEmployee(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists during creation")
    void createEmployee_WhenEmailExists_ShouldThrowDuplicateEmailException() {
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

        given(employeeRepository.existsByEmail(request.email())).willReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jane.doe@example.com");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should return employee by ID when present")
    void getEmployeeById_WhenFound_ShouldReturnEmployee() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(sampleEmployee));

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee does not exist")
    void getEmployeeById_WhenNotFound_ShouldThrowException() {
        given(employeeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: '999'");
    }

    @Test
    @DisplayName("Should update employee successfully when valid request provided")
    void updateEmployee_WhenValid_ShouldUpdateAndReturnResponse() {
        UpdateEmployeeRequest updateRequest = new UpdateEmployeeRequest(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "Engineering",
                "Lead Engineer",
                BigDecimal.valueOf(110000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15)
        );

        given(employeeRepository.findById(1L)).willReturn(Optional.of(sampleEmployee));
        given(employeeRepository.existsByEmailAndIdNot(updateRequest.email(), 1L)).willReturn(false);
        given(employeeRepository.save(any(Employee.class))).willReturn(sampleEmployee);

        EmployeeResponse response = employeeService.updateEmployee(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(employeeRepository).save(sampleEmployee);
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when updating to an email taken by another employee")
    void updateEmployee_WhenEmailTakenByOther_ShouldThrowDuplicateEmailException() {
        UpdateEmployeeRequest updateRequest = new UpdateEmployeeRequest(
                "Jane",
                "Smith",
                "existing@example.com",
                "Engineering",
                "Lead Engineer",
                BigDecimal.valueOf(110000),
                EmployeeStatus.ACTIVE,
                LocalDate.of(2023, 1, 15)
        );

        given(employeeRepository.findById(1L)).willReturn(Optional.of(sampleEmployee));
        given(employeeRepository.existsByEmailAndIdNot("existing@example.com", 1L)).willReturn(true);

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateRequest))
                .isInstanceOf(DuplicateEmailException.class);

        verify(employeeRepository, never()).save(sampleEmployee);
    }

    @Test
    @DisplayName("Should delete employee when employee ID exists")
    void deleteEmployee_WhenExists_ShouldDelete() {
        given(employeeRepository.existsById(1L)).willReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent employee")
    void deleteEmployee_WhenNotFound_ShouldThrowException() {
        given(employeeRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
