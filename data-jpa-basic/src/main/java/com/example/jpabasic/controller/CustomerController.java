package com.example.jpabasic.controller;

import com.example.jpabasic.entity.Customer;
import com.example.jpabasic.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Customer CRUD operations.
 *
 * Endpoints:
 *   GET    /api/customers           — list all
 *   GET    /api/customers/{id}      — get by id
 *   GET    /api/customers/byName    — find by last name
 *   GET    /api/customers/search    — JPQL search (partial name match)
 *   GET    /api/customers/byEmail   — find by unique email
 *   POST   /api/customers           — create
 *   PUT    /api/customers/{id}      — update email
 *   DELETE /api/customers/{id}      — delete
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> listAll() {
        return customerService.findAll();
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @GetMapping("/byName")
    public List<Customer> getByLastName(@RequestParam String lastName) {
        return customerService.findByLastName(lastName);
    }

    @GetMapping("/search")
    public List<Customer> search(@RequestParam String term) {
        return customerService.searchByName(term);
    }

    @GetMapping("/byEmail")
    public Customer getByEmail(@RequestParam String email) {
        return customerService.findByEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Customer> create(@Valid @RequestBody CreateCustomerRequest request) {
        Customer created = customerService.createCustomer(
                request.firstName(), request.lastName(), request.email(), request.phone());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Customer updateEmail(@PathVariable Long id, @RequestBody UpdateEmailRequest request) {
        return customerService.updateEmail(id, request.email());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    // --- Request DTO records ---

    public record CreateCustomerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            String phone) {}

    public record UpdateEmailRequest(@NotBlank @Email String email) {}
}
