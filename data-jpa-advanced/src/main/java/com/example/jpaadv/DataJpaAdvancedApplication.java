package com.example.jpaadv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Advanced Spring Data JPA Demo Application.
 *
 * Demonstrates:
 * - Composite primary keys (@EmbeddedId)
 * - JPA Specifications for dynamic queries
 * - DTO projections (closed / interface-based)
 * - Pagination and Sorting (Page, Slice, Pageable)
 * - JPA Auditing with custom AuditorAware
 * - Entity lifecycle callbacks (@PostLoad, @PrePersist, @PostPersist)
 * - Optimistic locking (@Version)
 * - Pessimistic locking (@Lock)
 * - Native queries with result mapping
 * - EntityManager batch processing
 */
@SpringBootApplication
public class DataJpaAdvancedApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaAdvancedApplication.class, args);
    }
}
