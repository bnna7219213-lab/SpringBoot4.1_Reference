package com.example.procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestDTO {

    // Rename: prNo -> prNumber
    private String prNumber;

    // Auto
    private String applicant;

    // Department name (auto or lookup-based display)
    private String departmentDisplay;

    // JSON -> List<PurchaseItemDTO>
    private List<PurchaseItemDTO> items;

    // BigDecimal budget -> "¥123,456.00" with thousand separators
    private String totalBudgetFormatted;

    // Integer urgencyLevel -> Chinese text
    private String urgencyText;

    // JSON approvalChain -> "2/5" progress
    private String approvalProgress;

    // Integer status -> Chinese text
    private String statusText;

    // approvedBy + approvedAt -> "Bob Director @ 2024-01-14 16:30"
    private String approvedByAt;

    // Computed: expectedDeliveryDate -> days remaining
    private long deliveryDaysRemaining;

    // Computed: days remaining < 0 -> true
    private boolean overdueFlag;

    // Computed: urgency, days remaining -> compliance level indicator
    private String complianceIndicator;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemDTO {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
