package com.example.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOrderDTO {

    private Long id;

    // Rename: orderNo -> orderNumber
    private String orderNumber;

    // customerId,customerName combined -> customer reference
    private String customer;

    // Computed from customer name: display name (e.g., prefix "VIP-")
    private String customerDisplayName;

    // JSON items string -> List<MenuItemDTO>
    private List<MenuItemDTO> items;

    // BigDecimal cents -> "formatPrice"
    private String totalPrice;

    // Integer tableNumber -> "Table: A05"
    private String tableDisplay;

    // Computed: minutes between now and bookingTime
    private long waitMinutes;

    // String status -> badge with color and text
    private StatusBadgeDTO statusBadge;

    // Aggregated from items list
    private String itemSummary;

    // Computed: bookingTime > 20 minutes ago
    private boolean isUrgent;

    // Computed item summary (duplicate for reporting)
    private String itemSummaryReport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemDTO {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private String displaySubtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusBadgeDTO {
        private String color;
        private String bgColor;
        private String text;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDTO {
        private Long id;
        private String name;
        private boolean isVip;
    }
}
