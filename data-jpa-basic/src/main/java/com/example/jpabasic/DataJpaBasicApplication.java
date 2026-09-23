package com.example.jpabasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA Basics Demo Application.
 *
 * Demonstrates:
 * - Jakarta persistence annotations (@Entity, @Id, @GeneratedValue)
 * - Derived query methods in repositories
 * - @Query JPQL with named parameters
 * - JPA auditing with @CreatedDate/@LastModifiedDate
 * - @EntityGraph for fetch strategy optimization
 */
@SpringBootApplication
@EnableJpaAuditing
public class DataJpaBasicApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataJpaBasicApplication.class, args);
    }
}
