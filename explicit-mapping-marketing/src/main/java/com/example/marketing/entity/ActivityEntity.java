package com.example.marketing.entity;

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
public class ActivityEntity {

    private Long id;
    private String activityCode;
    private Long campaignId;
    private String variant;               // control/treatment_a/treatment_b
    private Integer participantCount;
    private Integer clickCount;
    private Integer conversionCount;
    private BigDecimal revenue;
    private BigDecimal costPerClick;
    private LocalDate startDate;
    private LocalDate endDate;
}
