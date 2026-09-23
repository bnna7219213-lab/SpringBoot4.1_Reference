# mybatis-plus-crud

Demonstrates MyBatis-Plus `IService`/`ServiceImpl` for single-table CRUD with pagination and batch operations.

## Technology Stack

- Spring Boot 4.1.0
- JDK 21
- MyBatis-Plus 3.5.7 (spring-boot3-starter, compatible with SB4)
- H2 In-Memory Database
- Lombok

## Key Features

### Entity Annotations
- `@TableName` - Maps class to database table
- `@TableId(type = IdType.AUTO)` - Auto-increment primary key
- `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)` - Auto-fill audit fields
- `@EnumValue` - Maps enum/integer fields to DB values
- `@TableLogic` - Enables logical delete (auto WHERE deleted=0)

### IService / ServiceImpl
- Full CRUD: save, saveBatch, updateById, removeById, list, page
- Type-safe LambdaQueryWrapper for dynamic queries
- Custom SQL via @Select annotations
- Custom service methods wrapping BaseMapper

### Configuration
- PaginationInnerInterceptor for pagination
- MetaObjectHandler for auto-filling createTime/updateTime
- Logic delete: deleted field auto-handled

## REST API Endpoints

| Method | Path                    | Description                 |
|--------|-------------------------|-----------------------------|
| POST   | /api/users              | Create user                 |
| PUT    | /api/users/{id}         | Update user                 |
| DELETE | /api/users/{id}         | Delete user (logical)       |
| GET    | /api/users/{id}         | Get user by ID              |
| GET    | /api/users              | Paginated list with filters |
| POST   | /api/users/batch        | Batch create users          |
| GET    | /api/users/search       | Fuzzy search by username    |

### Query Parameters for GET /api/users

- `pageNum` - Page number (default 1)
- `pageSize` - Page size (default 10)
- `username` - Filter by username (LIKE)
- `status` - Filter by status (1=active, 0=inactive)
- `minAge` - Minimum age
- `maxAge` - Maximum age

## Running

```bash
mvn spring-boot:run
```

Access H2 Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:mpcrud`
- Username: `sa`
- Password: (empty)
