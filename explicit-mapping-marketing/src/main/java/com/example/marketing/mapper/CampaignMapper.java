package com.example.marketing.mapper;

import com.example.marketing.converter.MarketingConverters;
import com.example.marketing.dto.CampaignDTO;
import com.example.marketing.entity.CampaignEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Mapper(componentModel = "spring", uses = MarketingConverters.class)
public interface CampaignMapper {

    @Mapping(source = "typeId", target = "typeText", qualifiedByName = "typeIdToText")
    @Mapping(source = "status", target = "statusText", qualifiedByName = "statusToText")
    @Mapping(source = "conversionRate", target = "conversionDisplay", qualifiedByName = "conversionRateToDisplay")
    @Mapping(target = "budgetDisplay", ignore = true)
    @Mapping(target = "spendPercent", ignore = true)
    @Mapping(target = "runningDisplay", ignore = true)
    @Mapping(target = "platformsDisplay", ignore = true)
    @Mapping(target = "targetAudienceDisplay", ignore = true)
    @Mapping(target = "isLive", ignore = true)
    @Mapping(target = "organizerDisplay", ignore = true)
    CampaignDTO toCampaignDTO(CampaignEntity entity);

    @AfterMapping
    default void fillComputedFields(CampaignEntity entity, @MappingTarget CampaignDTO dto) {
        // Computed: spendPercent = actualSpend/budget * 100
        dto.setSpendPercent(MarketingConverters.computeSpendPercent(entity.getActualSpend(), entity.getBudget()));

        // Formatting: budget display
        dto.setBudgetDisplay(MarketingConverters.bigDecimalToYuanDisplay(entity.getBudget()));

        // Merge: startTime + endTime -> Chinese date range
        dto.setRunningDisplay(MarketingConverters.dateRangeToDisplay(entity.getStartTime(), entity.getEndTime()));

        // CSV -> structured display
        dto.setPlatformsDisplay(MarketingConverters.csvToPlatformsDisplay(entity.getPlatforms()));

        // Computed: isLive from status + time range
        dto.setLive(MarketingConverters.computeIsLive(entity.getStatus(), entity.getStartTime(), entity.getEndTime()));

        // JSON parse -> display
        if (entity.getTargetAudienceJson() != null) {
            dto.setTargetAudienceDisplay(simplifyJson(entity.getTargetAudienceJson()));
        }

        // organizerId -> display name
        dto.setOrganizerDisplay("User#" + entity.getOrganizerId());
    }

    private static String simplifyJson(String json) {
        if (json == null) return "{}";
        return json.replaceAll("[{}\"]", "").replace(":", "=").replace(",", ", ");
    }
}
