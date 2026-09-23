package com.example.procurement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestEntity {

    private Long id;
    private String prNo;
    private String applicantName;
    private String department;
    private String itemsJson;              // JSON array of items
    private BigDecimal totalBudget;
    private Integer urgencyLevel;           // 0=normal, 1=expedite, 2=urgent, 3=flash
    private String approvalChainJson;       // JSON workflow stages
    private Integer status;                 // 0=draft, 1=in_review, 2=approved, 3=rejected
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDate expectedDeliveryDate;
}
