package com.example.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO {

    // variant String -> "A/B Testing Variant: Treatment B"
    private String variantDisplay;

    // Auto
    private Integer participantCount;
    private Integer clickCount;
    private Integer conversionCount;

    // Auto (BigDecimal)
    private String revenueDisplay;

    // Computed: clickCount/participantCount as double
    private double ctr;

    // Computed: conversionCount/clickCount as double
    private double cvr;

    // Computed: revenue/cost as double
    private double roi;

    // BigDecimal costPerClick -> "formatPrice" string
    private String cpcDisplay;

    // Computed: endDate - startDate days
    private long durationDays;

    // Computed weighted score: 0.3*cvr + 0.4*ctr + 0.3*roi_normalized
    private double performanceScore;
}
