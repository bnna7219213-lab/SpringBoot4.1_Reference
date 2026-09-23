# explicit-mapping-logistics

Logistics domain: Shipment/Package tracking with GPS coordinates and timelines.

## Domain Mapping Challenges

All converters here show FAILING-BY-DEFAULT with BeanUtils — silently producing null.

| # | Converter | Description | Mismatch Type | BeanUtils |
|---|-----------|-------------|---------------|-----------|
| 1 | maskPhone | "1*********4" -> "138****1234" | Business rule | null |
| 2 | stateToStatusText | Integer state -> Chinese text | Type+Semantic | null |
| 3 | haversineDistance | 4 GPS fields -> km string | Computed | null |
| 4 | formatCoords | lat/lng -> "39.9042°N, 116.4074°E" | Multi-field + formatting | null |
| 5 | eventLevelToIcon | Integer level -> symbol char | Type+Semantic | null |
| 6 | stateToDelivered | Integer 3 -> boolean true | Type | false (default) |
| 7 | dateTimeToShortStr | LocalDateTime -> "2024-01-15 09:00" | Formatting | null |

### Mapping Plan (ShipmentDTO: 8 fields)

| Source Field(s) | Target Field | Auto? | Config |
|---|---|---|---|
| shipmentCode | shipmentCode | YES | — |
| senderName + senderPhone(mask) | senderInfo | NO | @AfterMapping merge |
| receiverName + receiverPhone | receiverInfo | NO | @AfterMapping merge |
| currentState | statusText | NO | @Named("stateToStatusText") |
| senderLat/Lng + receiverLat/Lng | route | NO | @AfterMapping Haversine |
| price | formattedPrice | NO | @Named("formatPrice") |
| timeline | timeline | NO | List mapping |
| eventTime (last) | lastEventTimeStr | NO | @AfterMapping |

## API Endpoints

| Method | URL | Response |
|--------|-----|----------|
| GET | `/api/shipments` | List<ShipmentDTO> |

## Running

```bash
mvn spring-boot:run
# Default: http://localhost:9002
```
