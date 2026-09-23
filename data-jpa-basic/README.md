# data-jpa-basic

Spring Data JPA Basics demonstration — repository, derived queries, @Entity, auditing, and basic CRUD.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- Spring Data JPA (Jakarta Persistence API)
- H2 in-memory database
- Spring Web (REST Controller)

## Project Structure

```
src/main/java/com/example/jpabasic/
├── DataJpaBasicApplication.java    # @SpringBootApplication + @EnableJpaAuditing
├── entity/
│   └── Customer.java               # JPA entity with Jakarta annotations
├── repository/
│   └── CustomerRepository.java     # JpaRepository + derived + @Query
├── service/
│   └── CustomerService.java        # @Transactional service layer
└── controller/
    └── CustomerController.java     # REST controller with request DTOs
```

## Key Concepts Demonstrated

### 1. Jakarta Namespace (JPA 3.x / Jakarta EE 9+)

Spring Boot 4 uses the Jakarta namespace — all persistence annotations come from `jakarta.persistence.*`
rather than the legacy `javax.persistence.*`. This is a breaking change from Spring Boot 2.x/3.x.

```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
```

### 2. Spring Data JPA Repositories

Repository interfaces extend `JpaRepository<T, ID>` and Spring generates implementations at runtime:

- **Derived queries**: `findByLastName`, `findByFirstNameAndLastName` — method names parsed into JPQL
- **@Query JPQL**: `searchByName` — custom query with `LIKE` and named parameters
- **@EntityGraph**: `findDetailedById` — overrides default fetch strategy for a specific query

### 3. JPA Auditing

```java
@EnableJpaAuditing                    // in main class
@CreatedDate / @LastModifiedDate     // in entity
@EntityListeners(AuditingEntityListener.class) // hooks listener
```

Timestamps are automatically managed — no manual `setCreatedAt()` calls.

### 4. Transactional Service Layer

- `@Transactional(readOnly = true)` — for queries (disables dirty checking)
- `@Transactional` — for write operations (creates/update/delete)

## Running

```bash
mvn spring-boot:run
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/customers` | List all customers |
| GET | `/api/customers/{id}` | Get customer by ID |
| GET | `/api/customers/byName?lastName=Smith` | Find by last name (derived query) |
| GET | `/api/customers/search?term=ali` | JPQL partial name search |
| GET | `/api/customers/byEmail?email=alice@example.com` | Find by unique email |
| POST | `/api/customers` | Create new customer |
| PUT | `/api/customers/{id}` | Update customer email |
| DELETE | `/api/customers/{id}` | Delete customer |

## Sample Requests

```bash
# List all customers (seeded from data.sql)
curl http://localhost:8080/api/customers

# Create a customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane@example.com","phone":"555-9999"}'

# Search by name (JPQL @Query)
curl http://localhost:8080/api/customers/search?term=smith

# Find by last name (derived query)
curl http://localhost:8080/api/customers/byName?lastName=Smith

# Update email
curl -X PUT http://localhost:8080/api/customers/1 \
  -H "Content-Type: application/json" \
  -d '{"email":"alice.new@example.com"}'
```

## H2 Console

Access at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:jpabasicdb`
- Username: `sa`
- Password: (empty)
