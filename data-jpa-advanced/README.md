# data-jpa-advanced

Advanced Spring Data JPA demonstration — composite keys, specifications, projections, pagination, auditing, and locking.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- Spring Data JPA (Jakarta Persistence API 3.x / Hibernate 7)
- H2 in-memory database

## Key Concepts Demonstrated

### 1. Composite Primary Keys (@EmbeddedId)
- `OrderItemId` — @Embeddable class implementing Serializable with equals/hashCode
- `OrderItem` uses @EmbeddedId + @MapsId for join-based composite key pattern

### 2. JPA Relationships
- `Order.OneToMany(OrderItem)` with cascade=ALL and orphanRemoval=true
- `OrderItem.ManyToOne(Product)` — ManyToOne with @MapsId mapping composite key part

### 3. JPA Specifications (Dynamic Queries)
- `OrderSpecification` provides composable WHERE conditions
- Uses CriteriaBuilder for type-safe dynamic query construction
- Joins (status IN, product contains, date ranges, aggregate HAVING)

### 4. DTO Projections
- Closed projection `OrderSummary` class
- JPQL `SELECT new ...` constructor expression instantiates DTOs directly
- Avoids loading the full entity graph

### 5. Pagination & Sorting
- `Page<T>` — full pagination with COUNT query
- `Slice<T>` — efficient scroll pagination (no COUNT)
- Sort by multiple fields with Sort.by()

### 6. JPA Auditing
- `@EnableJpaAuditing` + custom `AuditorAware<String>` in `JpaConfig`
- `@CreatedDate` / `@LastModifiedDate` auto-populated

### 7. Entity Lifecycle Callbacks
- `@PrePersist`: validate + set defaults before INSERT
- `@PostPersist`: post-creation side effects
- `@PreUpdate`: recompute derived fields before UPDATE
- `@PostLoad`: initialize transient state after SELECT

### 8. Locking Strategies
- **Optimistic Locking** (`@Version`): concurrent modification detection
  - Retry logic catches `ObjectOptimisticLockingFailureException`
- **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`):
  - `SELECT ... FOR UPDATE` blocks concurrent access
  - Configurable lock timeout via `@QueryHint`

### 9. Native Queries
- `findOrderStatisticsNative()` — SQL GROUP BY aggregation
- `updateOrderStatusNative()` — SQL UPDATE (requires @Modifying)

### 10. Batch Processing
- EntityManager-based batch updates with periodic flush/clear
- Prevents OutOfMemoryError for large datasets

## Project Structure

```
src/main/java/com/example/jpaadv/
├── DataJpaAdvancedApplication.java  # @SpringBootApplication
├── config/
│   └── JpaConfig.java               # @EnableJpaAuditing + AuditorAware
├── entity/
│   ├── Order.java                   # OneToMany, @Version, lifecycle callbacks
│   ├── OrderItem.java               # Composite key entity with @MapsId
│   ├── OrderItemId.java             # @Embeddable composite key class
│   └── Product.java                 # @Version for optimistic locking
├── repository/
│   ├── OrderRepository.java         # JpaSpecExecutor + native + projection + lock
│   └── ProductRepository.java       # Pessimistic lock queries
├── spec/
│   └── OrderSpecification.java      # Dynamic WHERE predicates
├── projection/
│   └── OrderSummary.java            # DTO projection for JPQL NEW
├── service/
│   └── OrderService.java            # Pessimistic/optimistic locking + batch
└── controller/
    └── OrderController.java         # REST endpoints
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/orders?orderNo=...&productId=...&quantity=...` | Create order (pessimistic lock) |
| POST | `/api/orders/optimistic?...` | Create order (optimistic lock + retry) |
| GET | `/api/orders/summaries?status=PAID` | DTO projection query |
| GET | `/api/orders/summariesPaged?page=0&size=10` | Paged DTO projection |
| GET | `/api/orders/scroll?page=0&size=10` | Slice-based pagination |
| GET | `/api/orders/statistics` | Native SQL aggregation |
| GET | `/api/orders/byCustomerPaged?customerName=...` | Paged order query |

## Running

```bash
mvn spring-boot:run
```

H2 Console: http://localhost:8081/h2-console
