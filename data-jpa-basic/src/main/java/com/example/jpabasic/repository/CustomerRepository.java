package com.example.jpabasic.repository;

import com.example.jpabasic.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Customer.
 *
 * QUERY METHODS DEMONSTRATED:
 *
 * 1. Derived queries — method name parsed into JPQL automatically:
 *    findByLastName, findByEmail, findByFirstNameAndLastName
 *
 * 2. @Query (JPQL) — custom query with named parameter binding
 *
 * 3. @EntityGraph — overrides LAZY loading for a specific query to avoid N+1
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // --- Derived query methods (Spring derives the query from method name) ---

    /**
     * Find all customers sharing a last name.
     * Generated SQL: SELECT ... FROM customer WHERE last_name = ?
     */
    List<Customer> findByLastName(String lastName);

    /**
     * Find a unique customer by email (enforced by UNIQUE constraint at DB level).
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customers matching both first AND last name.
     * Generated SQL: SELECT ... FROM customer WHERE first_name = ? AND last_name = ?
     */
    List<Customer> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find customers whose last name contains a substring (case-insensitive).
     */
    List<Customer> findByLastNameContainingIgnoreCase(String fragment);

    // --- @Query JPQL ---

    /**
     * Custom JPQL query: search by partial match on first or last name.
     * Named parameter :searchTerm is bound at runtime.
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "ORDER BY c.lastName, c.firstName")
    List<Customer> searchByName(@Param("term") String searchTerm);

    /**
     * JPQL query to retrieve only id and email for lightweight projections.
     * Returns Object[] tuples (id, email).
     */
    @Query("SELECT c.id, c.email FROM Customer c ORDER BY c.id")
    List<Object[]> findAllIdAndEmail();

    // --- EntityGraph for fetch strategy ---

    /**
     * If Customer had lazy-loaded relations, @EntityGraph forces a JOIN FETCH
     * to load them eagerly for this specific query call.
     * Example here for documentation purposes — illustrates the pattern.
     */
    @EntityGraph(attributePaths = {"firstName", "lastName", "email"})
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findDetailedById(@Param("id") Long id);

    /**
     * Count customers by last name prefix.
     */
    long countByLastNameStartingWith(String prefix);
}
