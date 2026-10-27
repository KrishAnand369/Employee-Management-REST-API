package com.krish.employeemanagement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_email", columnList = "email", unique = true),
        @Index(name = "idx_employee_department", columnList = "department"),
        @Index(name = "idx_employee_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "department", nullable = false, length = 50)
    private String department;

    @Column(name = "position", nullable = false, length = 80)
    private String position;

    @Column(name = "salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Employee(String firstName,
                    String lastName,
                    String email,
                    String department,
                    String position,
                    BigDecimal salary,
                    EmployeeStatus status,
                    LocalDate hireDate) {
        this.firstName = Objects.requireNonNull(firstName, "First name is required");
        this.lastName = Objects.requireNonNull(lastName, "Last name is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.department = Objects.requireNonNull(department, "Department is required");
        this.position = Objects.requireNonNull(position, "Position is required");
        this.salary = Objects.requireNonNull(salary, "Salary is required");
        this.status = status != null ? status : EmployeeStatus.ACTIVE;
        this.hireDate = hireDate != null ? hireDate : LocalDate.now();
    }

    public void updateDetails(String firstName,
                              String lastName,
                              String department,
                              String position,
                              BigDecimal salary,
                              EmployeeStatus status,
                              LocalDate hireDate) {
        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }
        if (department != null && !department.isBlank()) {
            this.department = department;
        }
        if (position != null && !position.isBlank()) {
            this.position = position;
        }
        if (salary != null) {
            this.salary = salary;
        }
        if (status != null) {
            this.status = status;
        }
        if (hireDate != null) {
            this.hireDate = hireDate;
        }
    }

    public void changeEmail(String newEmail) {
        this.email = Objects.requireNonNull(newEmail, "Email is required");
    }

    public void changeStatus(EmployeeStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "Status is required");
    }
}
