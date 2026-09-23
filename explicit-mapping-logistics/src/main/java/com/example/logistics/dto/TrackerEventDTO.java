package com.example.logistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerEventDTO {

    // LocalDateTime eventTime -> "2024-01-15 09:00" string
    private String timeStr;

    // Integer eventLevel -> symbol character
    private String locationIcon;

    // String locationDesc
    private String locationDescription;

    // BigDecimal lat, lng -> "39.9042°N, 116.4074°E"
    private String formattedCoords;
}
