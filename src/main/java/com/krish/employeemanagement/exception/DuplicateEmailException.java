package com.krish.employeemanagement.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super(String.format("An employee with email '%s' already exists", email));
    }
}
