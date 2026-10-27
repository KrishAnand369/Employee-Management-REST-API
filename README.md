# Employee Management REST API

A production-ready, clean-layered Spring Boot RESTful API developed for managing employee lifecycle records, departments, and employment status. 

Built with **Java 17**, **Spring Boot 3.3.5**, **Spring Data JPA**, **PostgreSQL**, and **OpenAPI 3 / Swagger UI**.

---

## Table of Contents
1. [Architecture & Design Decisions](#architecture--design-decisions)
2. [Project Structure](#project-structure)
3. [Technology Stack](#technology-stack)
4. [Prerequisites & Setup](#prerequisites--setup)
5. [Database Configuration](#database-configuration)
6. [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
7. [Endpoints & Sample Requests](#endpoints--sample-requests)
8. [Validation & Error Handling](#validation--error-handling)
9. [Running Automated Tests](#running-automated-tests)
10. [Postman Collection](#postman-collection)

---

## Architecture & Design Decisions

This project follows a **Clean Layered Architecture** with strict separation of concerns, adhering to idiomatic Spring Boot conventions:

```
[ HTTP Request / Swagger / Postman ]
                 │
                 ▼
        [ EmployeeController ]          <-- Validation, HTTP semantics, OpenAPI annotations
                 │
                 ▼
          [ EmployeeService ]           <-- Business logic, duplicate checks, transaction boundaries
                 │
                 ▼
        [ EmployeeRepository ]          <-- Spring Data JPA & Dynamic Specifications
                 │
                 ▼
       [ PostgreSQL Database ]
```

### Key Technical Choices
* **Clean Layered Architecture over DDD / Hexagonal**: Selected to match the domain complexity of a single entity CRUD application, keeping the codebase maintainable, readable, and free of unnecessary abstraction layers (ports/adapters, internal domain events) while adhering to the core assessment instructions.
* **Java 17 Records for DTOs**: Request and response objects (`CreateEmployeeRequest`, `UpdateEmployeeRequest`, `EmployeeResponse`) are immutable records, preventing accidental state mutation.
* **Encapsulated Entity**: The `Employee` JPA entity avoids open public setters, guarding domain integrity via explicit mutator methods (`updateDetails`, `changeEmail`, `changeStatus`).
* **Optimistic Locking (`@Version`)**: The entity includes a `@Version` attribute to prevent concurrent updates from overwriting changes (Lost Update problem). If two transactions conflict, an `OptimisticLockingFailureException` is trapped and returned as HTTP `409 Conflict`.
* **Dynamic Search & Filtering**: Implemented using Spring Data JPA `Specification` (`EmployeeSpecification`) allowing dynamic combinations of:
  * Case-insensitive keyword search across `firstName`, `lastName`, and `email`.
  * Filter by `department`.
  * Filter by `status` (`ACTIVE`, `INACTIVE`, `PROBATION`, `TERMINATED`).
* **Automated Auditing**: Entity lifecycle events are automatically timestamped with `@CreatedDate` and `@LastModifiedDate`.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/krish/employeemanagement/
│   │   ├── config/              # OpenAPI / Swagger configuration
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/          # REST Controllers with OpenAPI @Operation & responses
│   │   │   └── EmployeeController.java
│   │   ├── dto/                 # Java 17 immutable Records with Bean Validation
│   │   │   ├── CreateEmployeeRequest.java
│   │   │   ├── UpdateEmployeeRequest.java
│   │   │   ├── EmployeeResponse.java
│   │   │   └── EmployeeFilterCriteria.java
│   │   ├── entity/              # Domain entities and enums
│   │   │   ├── Employee.java
│   │   │   └── EmployeeStatus.java
│   │   ├── exception/           # Custom exceptions & global exception handler
│   │   │   ├── DuplicateEmailException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── ErrorResponse.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── repository/         # Spring Data JPA repositories & dynamic specifications
│   │   │   ├── EmployeeRepository.java
│   │   │   └── EmployeeSpecification.java
│   │   ├── service/            # Business logic interface and implementation
│   │   │   ├── EmployeeService.java
│   │   │   └── EmployeeServiceImpl.java
│   │   └── EmployeeManagementApplication.java
│   └── resources/
│       └── application.yaml     # Application configuration & PostgreSQL datasource
└── test/
    ├── java/com/krish/employeemanagement/
    │   ├── controller/EmployeeControllerTest.java    # WebMvc slice tests (validation & HTTP codes)
    │   ├── service/EmployeeServiceTest.java          # Mockito unit tests (business rules)
    │   └── EmployeeIntegrationTest.java              # End-to-end integration tests
    └── resources/
        └── application.yaml     # In-memory H2 configuration for automated tests
```

---

## Technology Stack

* **Language**: Java 17 LTS
* **Framework**: Spring Boot 3.3.5
* **Persistence**: Spring Data JPA / Hibernate 6
* **Database**: PostgreSQL 16 (H2 for in-memory testing)
* **Validation**: Jakarta Bean Validation (`hibernate-validator`)
* **API Documentation**: SpringDoc OpenAPI 2.6.0 (Swagger UI)
* **Testing**: JUnit 5, Mockito, AssertJ, Spring MockMvc
* **Containerization**: Docker Compose

---

## Prerequisites & Setup

### 1. Prerequisites
* **Java 17+** installed (`java -version`)
* **Docker & Docker Compose** installed (optional if you have local PostgreSQL)

### 2. Start PostgreSQL via Docker Compose
Run the provided Docker Compose file from the repository root:
```bash
docker compose up -d
```
This spins up a PostgreSQL 16 database pre-configured on port `5432`.

### 3. Run the Application
Using the Maven wrapper:

**On Linux/macOS:**
```bash
./mvnw spring-boot:run
```

**On Windows (PowerShell / Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## Database Configuration

Default connection parameters configured in `src/main/resources/application.yaml`:

| Property | Default Value | Environment Variable Override |
| :--- | :--- | :--- |
| **URL** | `jdbc:postgresql://localhost:5432/employee_db` | `SPRING_DATASOURCE_URL` |
| **Username** | `postgres` | `SPRING_DATASOURCE_USERNAME` |
| **Password** | `postgres` | `SPRING_DATASOURCE_PASSWORD` |
| **DDL Auto** | `update` | `spring.jpa.hibernate.ddl-auto` |

> **Note**: Automated unit and integration tests automatically run against an in-memory H2 database (`MODE=PostgreSQL`), so tests do not require an active PostgreSQL instance.

---

## API Documentation (Swagger UI)

Interactive OpenAPI 3 documentation is generated automatically and decorated with `@Operation` summaries, descriptions, and schemas.

* **Swagger UI URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Raw Spec (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Endpoints & Sample Requests

All routes are prefixed with `/api/v1/employees`.

| Method | Endpoint | Summary |
| :--- | :--- | :--- |
| `POST` | `/api/v1/employees` | Create a new employee |
| `GET` | `/api/v1/employees` | List employees (paginated, sorted, filtered, searchable) |
| `GET` | `/api/v1/employees/{id}` | Get employee by unique ID |
| `PUT` | `/api/v1/employees/{id}` | Update employee details |
| `DELETE` | `/api/v1/employees/{id}` | Delete employee by ID |

---

### Sample cURL Commands & Responses

#### 1. Create an Employee
```bash
curl -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@company.com",
    "department": "Engineering",
    "position": "Senior Backend Engineer",
    "salary": 120000.00,
    "status": "ACTIVE",
    "hireDate": "2024-01-15"
  }'
```

**Response (`201 Created` with `Location: /api/v1/employees/1`)**:
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@company.com",
  "department": "Engineering",
  "position": "Senior Backend Engineer",
  "salary": 120000.00,
  "status": "ACTIVE",
  "hireDate": "2024-01-15",
  "version": 0,
  "createdAt": "2024-01-15T09:30:00Z",
  "updatedAt": "2024-01-15T09:30:00Z"
}
```

---

#### 2. Get Employee by ID
```bash
curl -X GET http://localhost:8080/api/v1/employees/1
```

**Response (`200 OK`)**:
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@company.com",
  "department": "Engineering",
  "position": "Senior Backend Engineer",
  "salary": 120000.00,
  "status": "ACTIVE",
  "hireDate": "2024-01-15",
  "version": 0,
  "createdAt": "2024-01-15T09:30:00Z",
  "updatedAt": "2024-01-15T09:30:00Z"
}
```

---

#### 3. List Employees with Pagination & Filters
Supports `search`, `department`, `status`, `page`, `size`, and `sort`:
```bash
curl -X GET "http://localhost:8080/api/v1/employees?search=Jane&department=Engineering&status=ACTIVE&page=0&size=10&sort=lastName,asc"
```

**Response (`200 OK`)**:
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane.doe@company.com",
      "department": "Engineering",
      "position": "Senior Backend Engineer",
      "salary": 120000.00,
      "status": "ACTIVE",
      "hireDate": "2024-01-15",
      "version": 0,
      "createdAt": "2024-01-15T09:30:00Z",
      "updatedAt": "2024-01-15T09:30:00Z"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

#### 4. Update Employee
```bash
curl -X PUT http://localhost:8080/api/v1/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@company.com",
    "department": "Engineering",
    "position": "Staff Software Engineer",
    "salary": 140000.00,
    "status": "ACTIVE",
    "hireDate": "2024-01-15"
  }'
```

**Response (`200 OK`)**:
```json
{
  "id": 1,
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@company.com",
  "department": "Engineering",
  "position": "Staff Software Engineer",
  "salary": 140000.00,
  "status": "ACTIVE",
  "hireDate": "2024-01-15",
  "version": 1,
  "createdAt": "2024-01-15T09:30:00Z",
  "updatedAt": "2024-01-15T10:15:00Z"
}
```

---

#### 5. Delete Employee
```bash
curl -X DELETE http://localhost:8080/api/v1/employees/1
```

**Response (`204 No Content`)**

---

## Validation & Error Handling

All validation errors and exceptions produce a standardized JSON payload via `GlobalExceptionHandler` (`@RestControllerAdvice`).

### Sample Validation Failure (`400 Bad Request`)
If a request is submitted with missing or invalid fields:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/employees",
  "timestamp": "2026-10-27T15:30:00Z",
  "validationErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email format is invalid"
    },
    {
      "field": "salary",
      "rejectedValue": -500,
      "message": "Salary must be greater than zero"
    }
  ]
}
```

### Sample Duplicate Email Failure (`409 Conflict`)
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "An employee with email 'jane.doe@company.com' already exists",
  "path": "/api/v1/employees",
  "timestamp": "2026-10-27T15:30:00Z"
}
```

---

## Running Automated Tests

Run the full automated test suite (unit + slice + integration tests):

```bash
# Linux / macOS
./mvnw clean test

# Windows PowerShell
.\mvnw.cmd clean test
```

### Test Coverage Highlights
* **Unit Tests (`EmployeeServiceTest`)**: Mockito tests covering employee creation, uniqueness checks, not-found conditions, updates, and deletions.
* **Slice Tests (`EmployeeControllerTest`)**: Spring MVC MockMvc tests checking HTTP status codes, headers, and Bean Validation error responses.
* **Integration Tests (`EmployeeIntegrationTest`)**: End-to-end tests exercising full CRUD lifecycle, dynamic specifications, and persistence against an in-memory database.

---

## Postman Collection

A ready-to-import Postman collection is located in the repository at:
```
postman/Employee_Management_API.postman_collection.json
```

Import this file directly into Postman to test all endpoints with pre-populated payloads and environment variables (`baseUrl`, `employeeId`).
