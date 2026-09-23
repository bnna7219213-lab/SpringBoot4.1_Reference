package com.example.procurement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity {

    private Long id;
    private String supplierName;
    private String supplierCode;
    private String unifiedSocialCode;     // Chinese business registration number (18 chars)
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String bankName;
    private String bankAccount;
    private String qualificationLevel;     // A/B/C/D
    private Integer cooperationStatus;     // 0=inactive, 1=active, 2=suspended, 3=blacklisted
    private BigDecimal registeredCapital;
    private LocalDate establishedDate;
    private BigDecimal lastEvaluationScore;
}
