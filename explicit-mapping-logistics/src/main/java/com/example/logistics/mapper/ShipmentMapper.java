package com.example.logistics.mapper;

import com.example.logistics.converter.LogisticsConverters;
import com.example.logistics.dto.ShipmentDTO;
import com.example.logistics.dto.TrackerEventDTO;
import com.example.logistics.entity.ShipmentEntity;
import com.example.logistics.entity.TrackerEventEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * MapStruct mapper for ShipmentEntity -> ShipmentDTO.
 *
 * Demonstrates ALL logistics-specific mapping mismatches:
 * 1. Rename: —
 * 2. Type+semantic: currentState Integer -> statusText String
 * 3. Computed: route distance (Haversine on 4 fields)
 * 4. Formatting: BigDecimal price, BigDecimal lat/lng
 * 5. Multi-field merge: senderName + senderPhone (masked) -> senderInfo
 * 6. List mapping: List<TrackerEventEntity> -> List<TrackerEventDTO>
 * 7. @AfterMapping for computed fields
 */
@Mapper(componentModel = "spring",
        uses = LogisticsConverters.class)
public interface ShipmentMapper {

    @Mapping(source = "currentState", target = "statusText", qualifiedByName = "stateToStatusText")
    @Mapping(source = "currentState", target = "delivered", qualifiedByName = "stateToDelivered")
    @Mapping(source = "currentState", target = "isDelivered", qualifiedByName = "stateToDelivered")
    @Mapping(source = "price", target = "formattedPrice", qualifiedByName = "formatPrice")
    @Mapping(target = "senderInfo", ignore = true)
    @Mapping(target = "receiverInfo", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "lastEventTimeStr", ignore = true)
    @Mapping(target = "timeline", ignore = true)
    ShipmentDTO toShipmentDTO(ShipmentEntity entity);

    @Mapping(source = "eventLevel", target = "locationIcon", qualifiedByName = "eventLevelToIcon")
    @Mapping(source = "locationDesc", target = "locationDescription")
    @Mapping(source = "eventTime", target = "timeStr", qualifiedByName = "dateTimeToShortStr")
    @Mapping(target = "formattedCoords", ignore = true)
    TrackerEventDTO toTrackerEventDTO(TrackerEventEntity entity);

    @AfterMapping
    default void fillComputedFields(ShipmentEntity entity, @MappingTarget ShipmentDTO dto) {
        // Merge: senderName + masked senderPhone -> senderInfo
        String maskedPhone = maskPhone(entity.getSenderPhone());
        dto.setSenderInfo(entity.getSenderName() + " " + maskedPhone);

        // Merge: receiverName + receiverPhone -> receiverInfo
        dto.setReceiverInfo(entity.getReceiverName() + " " + entity.getReceiverPhone());

        // Computed: Haversine distance between sender and receiver
        if (entity.getSenderLat() != null && entity.getSenderLng() != null
                && entity.getReceiverLat() != null && entity.getReceiverLng() != null) {
            double distance = LogisticsConverters.haversineDistance(
                    entity.getSenderLat().doubleValue(), entity.getSenderLng().doubleValue(),
                    entity.getReceiverLat().doubleValue(), entity.getReceiverLng().doubleValue());
            dto.setRoute(String.format("%.0f km", distance));
        } else {
            dto.setRoute("N/A");
        }

        // Last event time from timeline
        if (entity.getTimeline() != null && !entity.getTimeline().isEmpty()) {
            var lastEvent = entity.getTimeline().get(entity.getTimeline().size() - 1);
            dto.setLastEventTimeStr(LogisticsConverters.dateTimeToShortStr(lastEvent.getEventTime()));
        }

        // List mapping: compute formatted coords for each event
        if (entity.getTimeline() != null) {
            dto.setTimeline(entity.getTimeline().stream()
                    .map(this::toEventDTOWithCoords)
                    .toList());
        }
    }

    private TrackerEventDTO toEventDTOWithCoords(TrackerEventEntity entity) {
        TrackerEventDTO dto = toTrackerEventDTO(entity);
        // Merge: lat + lng -> formattedCoords
        dto.setFormattedCoords(LogisticsConverters.formatCoords(entity.getLat(), entity.getLng()));
        return dto;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Named("formatPrice")
    default String formatPrice(BigDecimal price) {
        if (price == null) return "¥0.00";
        return "¥" + price.toPlainString();
    }
}
