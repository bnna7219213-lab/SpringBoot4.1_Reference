package com.example.logistics.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logistics-specific type converters.
 *
 * All of these would silently fail with BeanUtils (produce null) because they
 * require type conversion, business logic, or multi-source computation.
 */
@Mapper(componentModel = "spring")
public final class LogisticsConverters {

    /**
     * CONVERTER 1: Phone number masking
     * "1*********4" -> "138****1234"
     *
     * BeanUtils: SILENTLY NULL — no source field matches target (business rule)
     */
    @Named("maskPhone")
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * CONVERTER 2: Integer state -> Chinese text
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("stateToStatusText")
    public static String stateToStatusText(Integer state) {
        if (state == null) return "未知";
        return switch (state) {
            case 0 -> "已揽收";
            case 1 -> "运输中";
            case 2 -> "派送中";
            case 3 -> "已签收";
            case 4 -> "异常";
            default -> "未知";
        };
    }

    /**
     * CONVERTER 3: Haversine formula for GPS distance
     * senderLat + senderLng + receiverLat + receiverLng -> "1,943 km"
     *
     * BeanUtils: SILENTLY NULL — 4 fields -> 1 computed value (no match)
     */
    @Named("haversineDistance")
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * CONVERTER 4: Coordinates formatting
     * BigDecimal lat + BigDecimal lng -> "39.9042°N, 116.4074°E"
     *
     * BeanUtils: SILENTLY NULL — 2 fields -> 1 formatted string
     */
    @Named("formatCoords")
    public static String formatCoords(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) return "未知坐标";
        String latNS = lat.doubleValue() >= 0 ? "N" : "S";
        String lngEW = lng.doubleValue() >= 0 ? "E" : "W";
        return "%.4f°%s, %.4f°%s".formatted(
                Math.abs(lat.doubleValue()), latNS,
                Math.abs(lng.doubleValue()), lngEW);
    }

    /**
     * CONVERTER 5: Event level -> location icon symbol
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("eventLevelToIcon")
    public static String eventLevelToIcon(Integer eventLevel) {
        if (eventLevel == null) return "○";
        return switch (eventLevel) {
            case 0 -> "●";
            case 1 -> "▲";
            case 2 -> "■";
            default -> "○";
        };
    }

    /**
     * CONVERTER 6: Integer state -> boolean isDelivered
     * BeanUtils: SILENTLY false — Integer -> boolean
     */
    @Named("stateToDelivered")
    public static boolean stateToDelivered(Integer state) {
        return state != null && state == 3;
    }

    /**
     * CONVERTER 7: LocalDateTime -> formatted string
     * BeanUtils: SILENTLY NULL — different types
     */
    @Named("dateTimeToShortStr")
    public static String dateTimeToShortStr(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
