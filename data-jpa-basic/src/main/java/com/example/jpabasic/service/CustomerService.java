package com.example.jpabasic.service;

import com.example.jpabasic.entity.Customer;
import com.example.jpabasic.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer demonstrating transactional semantics.
 *
 * - WRITE methods: @transactional (read-write)
 * - READ methods: @Transactional(readOnly = true) — for performance (no dirty checking)
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // --- Read operations: readOnly = true ---

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Customer> findByLastName(String lastName) {
        return customerRepository.findByLastName(lastName);
    }

    @Transactional(readOnly = true)
    public List<Customer> searchByName(String searchTerm) {
        return customerRepository.searchByName(searchTerm);
    }

    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Email not found: " + email));
    }

    // --- Write operations: full transactional ---

    @Transactional
    public Customer createCustomer(String firstName, String lastName, String email, String phone) {
        Customer customer = new Customer(firstName, lastName, email, phone);
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateEmail(Long id, String newEmail) {
        Customer customer = findById(id);
        customer.setEmail(newEmail);
        // No explicit save needed — dirty checking flushes at transaction commit
        return customer;
    }

    @Transactional
    public Customer partialUpdate(Long id, String firstName, String lastName, String phone) {
        Customer customer = findById(id);
        if (firstName != null) customer.setFirstName(firstName);
        if (lastName != null) customer.setLastName(lastName);
        if (phone != null) customer.setPhone(phone);
        return customerRepository.saveAndFlush(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countByLastNamePrefix(String prefix) {
        return customerRepository.countByLastNameStartingWith(prefix);
    }
}
