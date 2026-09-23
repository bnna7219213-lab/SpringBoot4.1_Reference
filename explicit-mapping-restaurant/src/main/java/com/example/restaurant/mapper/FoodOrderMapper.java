package com.example.restaurant.mapper;

import com.example.restaurant.converter.RestaurantConverters;
import com.example.restaurant.dto.FoodOrderDTO;
import com.example.restaurant.entity.FoodOrderEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring", uses = RestaurantConverters.class)
public interface FoodOrderMapper {

    @Mapping(source = "orderNo", target = "orderNumber")
    @Mapping(source = "customerName", target = "customer")
    @Mapping(source = "totalAmount", target = "totalPrice", qualifiedByName = "centsToDisplayPrice")
    @Mapping(source = "tableNumber", target = "tableDisplay", qualifiedByName = "tableNumberToDisplay")
    @Mapping(source = "bookingTime", target = "waitMinutes", qualifiedByName = "bookingTimeToWaitMinutes")
    @Mapping(target = "customerDisplayName", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "statusBadge", ignore = true)
    @Mapping(target = "itemSummary", ignore = true)
    @Mapping(target = "isUrgent", ignore = true)
    @Mapping(target = "itemSummaryReport", ignore = true)
    FoodOrderDTO toFoodOrderDTO(FoodOrderEntity entity);

    @AfterMapping
    default void fillComputedFields(FoodOrderEntity entity, @MappingTarget FoodOrderDTO dto) {
        // customerDisplayName = prefix + name
        dto.setCustomerDisplayName("\u2B50 " + entity.getCustomerName());

        // Parse items JSON -> List<MenuItemDTO>
        if (entity.getItemsJson() != null && !entity.getItemsJson().isBlank()) {
            dto.setItems(parseItems(entity.getItemsJson()));
        }

        // status badge with color
        dto.setStatusBadge(toStatusBadge(entity.getStatus()));

        // itemSummary from items list
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            StringBuilder summary = new StringBuilder();
            for (FoodOrderDTO.MenuItemDTO item : dto.getItems()) {
                if (summary.length() > 0) summary.append(" + ");
                summary.append(item.getName()).append(" x").append(item.getQuantity());
            }
            dto.setItemSummary(summary.toString());
            dto.setItemSummaryReport(summary.toString());
        }

        // isUrgent: wait > 20 minutes
        if (entity.getBookingTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(entity.getBookingTime(), LocalDateTime.now());
            dto.setUrgent(minutes > 20);
        }
    }

    private static String statusToColor(String status) {
        if (status == null) return "#94a3b8";
        return switch (status) {
            case "CREATED" -> "#3b82f6";
            case "PREPARING" -> "#f59e0b";
            case "READY" -> "#10b981";
            case "SERVED" -> "#8b5cf6";
            case "CANCELLED" -> "#ef4444";
            default -> "#94a3b8";
        };
    }

    private static FoodOrderDTO.StatusBadgeDTO toStatusBadge(String status) {
        String text = switch (status != null ? status : "") {
            case "CREATED" -> "Created";
            case "PREPARING" -> "Preparing";
            case "READY" -> "Ready";
            case "SERVED" -> "Served";
            case "CANCELLED" -> "Cancelled";
            default -> "Unknown";
        };
        return FoodOrderDTO.StatusBadgeDTO.builder()
                .color(statusToColor(status))
                .bgColor(statusToColor(status) + "20")
                .text(text)
                .icon("\u23F3")
                .build();
    }

    /**
     * Parse simple JSON items array. In production, use Jackson ObjectMapper.
     * Format: [{"name":"Mapo Tofu","qty":2,"price":1999},...]
     */
    private static List parseItems(String itemsJson) {
        try {
            // Simplified parsing — assumes well-formed JSON and uses manual extraction
            // In production, use objectMapper.readValue(itemsJson, new TypeReference<List<MenuItem>>(){})
            StringBuilder name = new StringBuilder();
            int quantity = 0;
            BigDecimal unitPrice = BigDecimal.ZERO;
            java.util.ArrayList itemList = new java.util.ArrayList();

            // Skip outer brackets
            String inner = itemsJson.trim();
            if (inner.startsWith("[")) inner = inner.substring(1);
            if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);

            // Simple parsing: find "name":"value" patterns
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "\"name\":\\s*\"([^\"]+)\"[^}]*\"qty\":\\s*(\\d+)[^}]*\"price\":\\s*(\\d+)")
                    .matcher(itemsJson);
            while (m.find()) {
                itemList.add(FoodOrderDTO.MenuItemDTO.builder()
                        .name(m.group(1))
                        .quantity(Integer.parseInt(m.group(2)))
                        .unitPrice(new BigDecimal(m.group(3)))
                        .displaySubtotal("cents")
                        .build());
            }
            return itemList;
        } catch (Exception e) {
            return java.util.List.of();
        }
    }
}
