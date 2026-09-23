package com.example.procurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    // Direct auto
    private String supplierName;
    private String supplierCode;

    // MaskedTaxId: 18-char unifiedSocialCode -> "9144****XXXXX"
    private String maskedTaxId;

    // Multi-field merge: contactPerson + contactPhone
    private String contact;

    // Multi-field merge: bankName + masked bankAccount
    private String bankInfo;

    // qualificationLevel + description lookup
    private String levelScore;

    // Integer -> Chinese text
    private String statusText;

    // BigDecimal score -> "92.5 / 100" string
    private String lastScore;

    // Computed: level != D && status == 1
    private boolean isQualified;

    // Computed: now.year - establishedDate.year
    private long establishedYears;
}
