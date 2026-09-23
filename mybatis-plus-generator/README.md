# mybatis-plus-generator

Demonstrates MyBatis-Plus code generator: automatically generates entity/mapper/service/controller from database tables.

## Technology Stack

- Spring Boot 4.1.0
- JDK 21
- MyBatis-Plus 3.5.7 (Generator + Spring Boot 3 Starter)
- Freemarker Template Engine
- H2 In-Memory Database
- Lombok

## How to Run the Generator

```bash
# Option 1: Run from IDE
# Right-click CodeGenerator.java -> Run 'CodeGenerator.main()'

# Option 2: Command line
mvn compile exec:java -Dexec.mainClass="com.example.mpgen.CodeGenerator"

# Option 3: Maven plugin (add exec-maven-plugin to pom.xml)
mvn exec:java -Dexec.mainClass="com.example.mpgen.CodeGenerator"
```

## Generated Output

The generator creates files based on the `t_product` table in `schema.sql`:

| File | Path |
|------|------|
| Entity | `generated/entity/Product.java` |
| Mapper | `generated/mapper/ProductMapper.java` |
| Mapper XML | `generated/mapper/xml/ProductMapper.xml` |
| Service Interface | `generated/service/IProductService.java` |
| Service Impl | `generated/service/impl/ProductServiceImpl.java` |
| Controller | `generated/controller/ProductController.java` |

## Key Features Demonstrated

- **FastAutoGenerator** - Fluent API for code generation
- **Externalized Config** - `generator-config.yml` + `@ConfigurationProperties`
- **Freemarker Templates** - Custom template support
- **Entity with Lombok** - `@Data`, `@Accessors`
- **Active Record** - Entity extends model
- **REST Style** - `@RestController` controllers
- **Table Prefix** - `t_` removed from entity name
- **Logic Delete** - `@TableLogic` on deleted field
- **Auto-fill Fields** - createTime, updateTime

## Configuration Options

See `generator-config.yml` for all available configuration options.

See `GeneratorConfig.java` for Java binding class.

## Example Generated Files

The `resources/generated/` directory contains example output showing what the generator produces.
