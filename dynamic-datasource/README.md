# dynamic-datasource

Demonstrates dynamic-datasource-spring-boot-starter: multi-datasource switching, read-write splitting, sharding awareness.

## Technology Stack

- Spring Boot 4.1.0
- JDK 21
- MyBatis-Plus 3.5.7
- dynamic-datasource-spring-boot-starter 4.3.0 (com.baomidou)
- H2 (2 in-memory databases: master + slave)
- Lombok

## Architecture

```
     ┌─────────────────────────────────────────────┐
     │              Controller Layer                 │
     └─────────────────┬───────────────────────────┘
                       │
     ┌─────────────────▼───────────────────────────┐
     │              Service Layer                    │
     │                                               │
     │  @DS("master") ──────┐   @DS("slave") ────┐  │
     │  Write Operations    │   Read Operations   │  │
     └──────────────────────┼────────────────────┼──┘
                            │                    │
              ┌─────────────▼──────────┐ ┌──────▼─────────────┐
              │     MASTER (Write)      │ │    SLAVE (Read)     │
              │     H2: mem:masterdb    │ │    H2: mem:slavedb  │
              └────────────────────────┘ └────────────────────┘
```

## Key Features Demonstrated

### @DS Annotation
- `@DS("master")` - Explicit datasource routing (write operations)
- `@DS("slave")` - Explicit datasource routing (read operations)
- `@DS("#param")` - SpEL expression from method parameter (dynamic selection)

### DynamicDataSourceContextHolder
- Manual push/pop for runtime datasource switching
- Thread-local based (safe for concurrent requests)

### AOP-based Read-Write Split
- annotation combined with AOP for automatic switching
- Custom load balancing across multiple slaves

### Programmatic Configuration
- `DataSourceConfig` shows runtime datasource creation
- Loading configs from external sources (DB, config center)

## REST API Endpoints

| Method | Path | Datasource | Description |
|--------|------|------------|-------------|
| POST | /api/products | MASTER | Create product |
| PUT | /api/products/{id} | MASTER | Update product |
| DELETE | /api/products/{id} | MASTER | Delete product |
| GET | /api/products/{id} | SLAVE | Get product by ID |
| GET | /api/products | SLAVE | List all products |
| GET | /api/products/count | SLAVE | Count products |
| GET | /api/products/ds/{name}/{id} | Dynamic | SpEL-based selection |
| GET | /api/products/ds/manual/{id} | Manual | ContextHolder push/pop |
| POST | /api/products/demo/write-read | Both | Write master, read slave |

## Running

```bash
mvn spring-boot:run
```

Access H2 Console: http://localhost:8082/h2-console
- Master: `jdbc:h2:mem:masterdb`
- Slave: `jdbc:h2:mem:slavedb`
- Username: `sa`
- Password: (empty)

## Usage Examples

Create a product (writes to master):
```bash
curl -X POST http://localhost:8082/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"iPad Pro","category":"Electronics","price":6999,"stock":80,"status":1}'
```

Read product (reads from slave):
```bash
curl http://localhost:8082/api/products/1
```

Read from specific datasource:
```bash
curl http://localhost:8082/api/products/ds/master/1
curl http://localhost:8082/api/products/ds/slave/1
```
