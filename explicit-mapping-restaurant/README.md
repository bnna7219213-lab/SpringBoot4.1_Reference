# explicit-mapping-restaurant

Restaurant/food delivery domain: Menu/Order mapping with dietary tags, time-sensitive display,
currency unit conversion (cents→yuan), and JSON parsing.

## Domain Mapping Challenges

| # | Converter | Description | Mismatch Type | BeanUtils |
|---|-----------|-------------|---------------|-----------|
| 1 | centsToDisplayPrice | Integer/BigDecimal cents -> "formatPrice" string | Type+currency | null |
| 2 | csvToList | String "peanut,soy" -> List<String> | Type (String->List) | null |
| 3 | spiceLevelToChinese | Integer 0-3 -> Chinese text | Type+Semantic | null |
| 4 | categoryIdToDisplayName | Long id -> "emoji category" lookup | Business logic | null |
| 5 | priceToLevel | BigDecimal -> "cheap/moderate/premium" | Computed | null |
| 6 | bookingTimeToWaitMinutes | DateTime -> long (computed) | Computed | 0 |
| 7 | tableNumberToDisplay | Integer -> "Table: A05" | Format string | null |
| 8 | stockToAvailable | Integer -> boolean | Type | false |
| 9 | computeProfitMargin | BigDecimal,BigDecimal -> double % | Multi-field computed | 0.0 |
| 10 | dateTimeToFormattedStr | LocalDateTime -> String | Formatting | null |

### Mapping Plan (DishDTO: 10 fields)

| Source Field | Target Field | Auto? | Required Config |
|---|---|---|---|
| id | id | YES | — |
| categoryId | displayName | NO | @Named("categoryIdToDisplayName") |
| price | displayPrice | NO | @Named("centsToDisplayPrice") |
| price | priceLevel | NO | @Named("priceToLevel") |
| isSpicy | spiceLevel | NO | @Named("spiceLevelToChinese") |
| allergens | allergenTags | NO | @Named("csvToList") |
| stock | available | NO | @Named("stockToAvailable") |
| specs | specsDisplay | NO | @AfterMapping |
| (price,cost) | profitMargin | NO | @AfterMapping compute |
| dishName | thumbnailUrl | NO | @Named (generated URL) |

### Mapping Plan (FoodOrderDTO: 12 fields)

| Source Field(s) | Target Field | Auto? | Config |
|---|---|---|---|
| orderNo | orderNumber | NO | @Mapping rename |
| totalAmount | totalPrice | NO | @Named("centsToDisplayPrice") |
| tableNumber | tableDisplay | NO | @Named("tableNumberToDisplay") |
| bookingTime | waitMinutes | NO | @Named("bookingTimeToWaitMinutes") |
| customerName | customerDisplayName | NO | @AfterMapping |
| itemsJson | items | NO | @AfterMapping JSON parse |
| status | statusBadge | NO | @AfterMapping |
| items | itemSummary | NO | @AfterMapping aggregate |
| bookingTime | isUrgent | NO | @AfterMapping compute |

## API Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `/api/dishes` | List<DishDTO> |
| GET | `/api/dishes/{id}` | DishDTO |
| GET | `/api/foodOrders` | List<FoodOrderDTO> |

### Sample JSON response (DishDTO)

```json
{
  "id": 2001,
  "displayName": " Sichuan",
  "displayPrice": "¥19.99",
  "priceLevel": "moderate",
  "spiceLevel": "中辣",
  "allergenTags": ["peanut", "soy"],
  "profitMargin": 57.48
}
```

## Running

```bash
mvn spring-boot:run
# Default: http://localhost:9003
```
