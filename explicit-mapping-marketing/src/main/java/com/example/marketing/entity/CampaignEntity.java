package com.example.marketing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignEntity {

    private Long id;
    private String campaignName;
    private String campaignCode;
    private Integer typeId;              // 1=coupon, 2=full_reduction, 3=group_buy, 4=flash_sale
    private BigDecimal budget;
    private BigDecimal actualSpend;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String platforms;            // CSV: "app,web,mini"
    private String targetAudienceJson;   // JSON string
    private Integer status;              // 0=draft, 1=review, 2=live, 3=ended, 4=offline
    private Long organizerId;
    private BigDecimal conversionRate;   // e.g., 0.0345 for 3.45%
}
