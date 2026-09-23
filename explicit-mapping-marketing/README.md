# explicit-mapping-marketing

Marketing/Campaign domain: Campaign/Activity mapping with A/B variants,
performance metrics computation, and campaign ROI calculations.

## Domain Mapping Challenges

| # | Converter | Description | Mismatch Type | BeanUtils |
|---|-----------|-------------|---------------|-----------|
| 1 | typeIdToText | Integer -> Chinese text | Type+Semantic | null |
| 2 | statusToText | Integer -> Chinese text | Type+Semantic | null |
| 3 | bigDecimalToYuanDisplay | BigDecimal -> "¥X,XXX" | Formatting | null |
| 4 | computeSpendPercent | 2 BigDecimals -> "X.X%" | Computed | null |
| 5 | dateRangeToDisplay | 2 DateTimes -> Chinese range | Multi-field merge + format | null |
| 6 | csvToPlatformsDisplay | CSV -> structured display | Type+Semantic | null |
| 7 | conversionRateToDisplay | BigDecimal -> "X.XX%" | Formatting | null |
| 8 | variantToDisplay | String -> label with context | Business semantic | null |
| 9 | costPerClickToDisplay | BigDecimal -> "¥X.XX" | Formatting | null |
| 10 | computePerformanceScore | 3 doubles -> weighted score | Weighted computed | 0.0 |
| 11 | datesToDurationDays | 2 Dates -> long days | Multi-field computed | 0 |
| 12 | computeIsLive | (status,start,end) -> boolean | Multi-field computed | false |
| 13 | revenueToDisplay | BigDecimal -> "¥X,XXX" | Formatting | null |

### BeanUtils Failure Rate

- **CampaignDTO (10 fields)**: 2 auto (campaignName, campaignCode), 8 explicit = 80% fail
- **ActivityDTO (11 fields)**: 3 auto (participantCount, clickCount, conversionCount), 8 explicit = 73% fail

### API Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `/api/campaigns` | List<CampaignDTO> |
| GET | `/api/activities` | List<ActivityDTO> |

### Sample JSON Response (CampaignDTO)

```json
{
  "campaignName": "Spring Sale 2024",
  "typeText": "优惠券",
  "statusText": "进行中",
  "budgetDisplay": "¥20,000",
  "spendPercent": "61.7%",
  "runningDisplay": "2024年1月1日 ~ 2024年1月31日",
  "platformsDisplay": "APP(应用), Web(网页), Mini(小程序)",
  "isLive": true,
  "conversionDisplay": "3.45%"
}
```

### Sample JSON Response (ActivityDTO)

```json
{
  "variantDisplay": "A/B Testing Variant: Treatment B",
  "participantCount": 15000,
  "clickCount": 2400,
  "conversionCount": 180,
  "ctr": 0.16,
  "cvr": 0.075,
  "roi": 15.2,
  "performanceScore": 5.89
}
```

## Running

```bash
mvn spring-boot:run
# Default: http://localhost:9005
```
