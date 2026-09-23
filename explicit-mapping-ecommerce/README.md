# explicit-mapping-ecommerce

E-commerce domain demonstrating WHY explicit bean mapping (MapStruct) is necessary over
reflection-based BeanUtils.

## Domain: Product & Order E-commerce API

### Technologies

- Spring Boot 4.1.0, Java 21
- MapStruct 1.6.3 (annotation processor)
- Lombok 1.18.34

### The 5 Categories of Mapping Mismatch

| Category | Example | Entity Type | DTO Type | BeanUtils Result |
|----------|---------|-------------|----------|-----------------|
| 1. Rename | orderNo -> orderNumber | String | String | null |
| 2. Type+Semantic | status Integer -> statusText String | Integer(0/1/2) | String("待支付") | null |
| 3. Formatting | unitPrice -> formattedPrice | BigDecimal | String("formatPrice") | null |
| 4. Computed | (no source) -> payableAmount | — | BigDecimal(total-discount) | null |
| 5. Multi-field Merge | province+city+detail -> fullAddress | 3 Strings | 1 String | null |

### Mapping Plan (Product: 7 fields)

| Source Field | Target Field | Auto? | Required Config |
|---|---|---|---|
| name | name | YES | — |
| sku | sku | YES | — |
| unitPrice | formattedPrice | NO | @Named("priceToFormatted") |
| stock | inStock | NO | @Named("stockToInStock") |
| weight | weightDisplay | NO | @Named("weightToDisplay") |
| categoryCode | categoryName | NO | @Named("categoryCodeToName") |
| id | id | YES | — |

### Mapping Plan (Order: 12 fields)

| Source Field | Target Field | Auto? | Required Config |
|---|---|---|---|
| id | id | YES | — |
| totalAmount | totalAmount | YES | — |
| orderNo | orderNumber | NO | @Mapping(source, target) |
| discountAmount | discount | NO | @Mapping(source, target) |
| status | statusText | NO | @Named("statusToText") |
| createTime | createTimeStr | NO | @Named("dateTimeToFormatted") |
| payableAmount | (computed) | NO | @AfterMapping |
| province+city+detail | fullAddress | NO | @AfterMapping merge |
| items | items | NO | List type conversion |
| itemCountSummary | (aggregate) | NO | @AfterMapping aggregate |

### API Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `/api/products` | List<ProductDTO> |
| GET | `/api/products/{id}` | ProductDTO |
| GET | `/api/orders` | List<OrderDTO> |

### Running

```bash
mvn spring-boot:run
# Default: http://localhost:9001
```

### Expected BeanUtils Failures (silently wrong)

When `BeanUtils.copyProperties(entity, dto)` is called:
- `productDTO.getFormattedPrice()` → null (BigDecimal→String)
- `productDTO.isInStock()` → false (not stock>0)
- `productDTO.getCategoryName()` → null (lookup table)
- `orderDTO.getOrderNumber()` → null (rename)
- `orderDTO.getStatusText()` → null (Integer→String)
- `orderDTO.getFullAddress()` → null (3-field merge)
- `orderDTO.getPayableAmount()` → null (computation)
- `orderDTO.getItems()` → null (List type change)

## Why MapStruct?

MapStruct generates correct mapping code at compile time:
- Compile error if source field doesn't exist (typo detection)
- IDE autocomplete for `@Mapping` annotations
- Zero reflection overhead — pure Java method calls
- `@AfterMapping` for multi-source computations
