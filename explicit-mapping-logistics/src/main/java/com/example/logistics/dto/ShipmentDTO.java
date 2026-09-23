package com.example.logistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDTO {

    // Direct (auto)
    private String shipmentCode;

    // senderName + senderPhone (masked) merge -> senderInfo
    private String senderInfo;

    // receiverName + receiverPhone merge -> receiverInfo
    private String receiverInfo;

    // Integer currentState -> statusText (Chinese)
    private String statusText;

    // Haversine distance from sender/receiver coords -> route "1,943 km"
    private String route;

    // Formatting: BigDecimal price -> "formatPrice"
    private String formattedPrice;

    // List<TrackerEventEntity> -> List<TrackerEventDTO>
    private List<TrackerEventDTO> timeline;

    // Long LocalDateTime eventTime -> String timeStr (formatted)
    private String lastEventTimeStr;

    // Computed: delivered boolean from state
    private boolean isDelivered;
}
