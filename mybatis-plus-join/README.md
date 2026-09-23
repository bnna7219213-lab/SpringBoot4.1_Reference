# mybatis-plus-join

Demonstrates MyBatis-Plus-Join (community extension) for multi-table JOIN queries without writing SQL.

## Technology Stack

- Spring Boot 4.1.0
- JDK 21
- MyBatis-Plus 3.5.7
- mybatis-plus-join-boot-starter 1.5.2 (from com.github.yulichang)
- H2 In-Memory Database
- Lombok

## Key Concepts

### Data Model

```
Order (1) --- (N) OrderItem (N) --- (1) Product
```

### MPJLambdaJoinWrapper

The core API for type-safe JOIN queries. Inherits from MyBatis-Plus's `LambdaQueryWrapper` and adds join capabilities.

### Key JOIN Features

| Feature | API Method | Description |
|---------|------------|-------------|
| INNER JOIN | `.innerJoin(Class, col, val)` | Only matching rows |
| LEFT JOIN | `.leftJoin(Class, col, val)` | All from left, nulls from right |
| DTO Projection | `.selectAs(col, setter)` | Map column to DTO field |
| Aggregation | `.selectSum()`, `.selectCount()` | Aggregate functions |
| Pagination | `selectJoinList(page, class, wrapper)` | Paginated join results |

### Mapper Interface

```java
public interface OrderMapper extends MPJBaseMapper<Order> {}
```

`MPJBaseMapper` extends `BaseMapper` and adds `selectJoinList`, `selectJoinPage`, `selectJoinOne`.

## REST API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/orders/{id}/detail | 3-table JOIN: Order+Items+Product |
| GET | /api/orders/details | All orders with details |
| GET | /api/orders | Paginated orders with filters |
| GET | /api/orders/products/sales | LEFT JOIN + aggregation |
| GET | /api/orders/by-no/{no} | Query by order number |

## Running

```bash
mvn spring-boot:run
```

Access H2 Console: http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:mpjoin`
- Username: `sa`
- Password: (empty)
