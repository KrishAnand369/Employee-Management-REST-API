package com.krish.employeemanagement.repository;

import com.krish.employeemanagement.dto.EmployeeFilterCriteria;
import com.krish.employeemanagement.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecification {

    private EmployeeSpecification() {
    }

    public static Specification<Employee> withCriteria(EmployeeFilterCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // Search filter: matches firstName, lastName, or email (case-insensitive)
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String pattern = "%" + criteria.search().trim().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                );
                predicates.add(searchPredicate);
            }

            // Department filter (case-insensitive exact match)
            if (criteria.department() != null && !criteria.department().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("department")),
                        criteria.department().trim().toLowerCase()
                ));
            }

            // Status filter
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
