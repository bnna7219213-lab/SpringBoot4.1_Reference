# explicit-mapping-procurement

Procurement domain: Supplier/Purchase Request mapping with approval workflows,
business registration masking, and compliance indicator computation.

## Domain Mapping Challenges

| # | Converter | Description | Mismatch Type | BeanUtils |
|---|-----------|-------------|---------------|-----------|
| 1 | maskTaxId | 18-char code -> first 4 + **** + last 4 | Business rule | null |
| 2 | mergeBankInfo | bankName + bankAccount -> merged display | Multi-field merge | null |
| 3 | statusToText | Integer -> Chinese text | Type+Semantic | null |
| 4 | scoreToDisplay | BigDecimal -> "X.Y / 100" string | Formatting | null |
| 5 | computeIsQualified | (level,status) -> boolean | Multi-field computed | false |
| 6 | dateToYearsSince | LocalDate -> long years | Computed | 0 |
| 7 | urgencyToText | Integer -> Chinese urgency text | Type+Semantic | null |
| 8 | budgetToFormatted | BigDecimal -> "¥X,XXX.XX" | Formatting | null |
| 9 | mergeApprovedByAt | (name,datetime) -> merged string | Multi-field merge | null |
| 10 | dateToDaysRemaining | LocalDate -> days until delivery | Computed | 0 |
| 11 | computeCompliance | (urgency,days) -> indicator | Multi-field computed | null |

### BeanUtils Failure Rate

**SupplierDTO (8 fields)**: 2 auto (supplierName, supplierCode), 6 require explicit = 75% fail
**PurchaseRequestDTO (13 fields)**: 1 auto (applicant via name rename), 12 require explicit = 92% fail

### API Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `/api/suppliers` | List<SupplierDTO> |
| GET | `/api/purchaseRequests` | List<PurchaseRequestDTO> |

### Sample JSON Response (SupplierDTO)

```json
{
  "maskedTaxId": "9144****XXXXX",
  "bankInfo": "招商银行 ****1234",
  "levelScore": "A (Excellent)",
  "statusText": "合作中",
  "lastScore": "92.5 / 100",
  "isQualified": true,
  "establishedYears": 9
}
```

## Running

```bash
mvn spring-boot:run
# Default: http://localhost:9004
```
