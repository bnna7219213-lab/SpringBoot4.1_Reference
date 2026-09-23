package com.example.logistics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ShipmentEntity — simulates JPA-loaded logistics shipment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentEntity {

    private Long id;

    // Direct mapping (auto)
    private String shipmentCode;

    // Need explicit: phone masking + multi-field merge
    private String senderName;
    private String senderPhone;
    private BigDecimal senderLat;
    private BigDecimal senderLng;

    // Multi-field merge for display name
    private String receiverName;
    private String receiverPhone;
    private BigDecimal receiverLat;
    private BigDecimal receiverLng;

    // Integer state code -> Chinese text
    private Integer currentState;  // 0=PICKED_UP, 1=IN_TRANSIT, 2=OUT_FOR_DELIVERY, 3=DELIVERED, 4=EXCEPTION

    // Direct mapping (auto)
    private BigDecimal weight;
    private BigDecimal volume;

    // Requires formatting
    private BigDecimal price;

    // Type conversion: List<TrackerEventEntity> -> List<TrackerEventDTO>
    private List<TrackerEventEntity> timeline;

    private LocalDateTime createTime;
}
