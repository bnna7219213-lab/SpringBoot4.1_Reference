package com.example.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDTO {

    // Auto
    private String campaignName;
    private String campaignCode;

    // typeId Integer -> typeText Chinese
    private String typeText;

    // Integer status -> statusText Chinese
    private String statusText;

    // spend/budget percentage "61.7%" (computed)
    private String budgetDisplay;
    private String spendPercent;

    // startTime+endTime -> "2023年1月1日 ~ 2023年1月31日" (multi-field merge + format)
    private String runningDisplay;

    // CSV platforms -> List<PlatformDTO> with icon + label (type+semantic)
    private String platformsDisplay;

    // JSON -> parsed display string
    private String targetAudienceDisplay;

    // Computed: status==2 && now in start/end range
    private boolean isLive;

    // BigDecimal conversionRate -> "3.45%" string
    private String conversionDisplay;

    // organizerId -> display name
    private String organizerDisplay;
}
