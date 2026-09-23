package com.example.marketing.mapper;

import com.example.marketing.converter.MarketingConverters;
import com.example.marketing.dto.ActivityDTO;
import com.example.marketing.entity.ActivityEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", uses = MarketingConverters.class)
public interface ActivityMapper {

    @Mapping(source = "variant", target = "variantDisplay", qualifiedByName = "variantToDisplay")
    @Mapping(source = "revenue", target = "revenueDisplay", qualifiedByName = "revenueToDisplay")
    @Mapping(source = "costPerClick", target = "cpcDisplay", qualifiedByName = "costPerClickToDisplay")
    @Mapping(target = "ctr", ignore = true)
    @Mapping(target = "cvr", ignore = true)
    @Mapping(target = "roi", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    @Mapping(target = "performanceScore", ignore = true)
    ActivityDTO toActivityDTO(ActivityEntity entity);

    @AfterMapping
    default void fillComputedFields(ActivityEntity entity, @MappingTarget ActivityDTO dto) {
        // Computed: ctr = clickCount / participantCount
        double ctr = entity.getParticipantCount() != null && entity.getParticipantCount() > 0
                ? (double) (entity.getClickCount() != null ? entity.getClickCount() : 0) / entity.getParticipantCount()
                : 0.0;
        dto.setCtr(Math.round(ctr * 10000.0) / 10000.0);

        // Computed: cvr = conversionCount / clickCount
        double cvr = entity.getClickCount() != null && entity.getClickCount() > 0
                ? (double) (entity.getConversionCount() != null ? entity.getConversionCount() : 0) / entity.getClickCount()
                : 0.0;
        dto.setCvr(Math.round(cvr * 10000.0) / 10000.0);

        // Computed: roi = revenue / (cpc * clickCount)
        double totalCpc = (entity.getCostPerClick() != null && entity.getClickCount() != null)
                ? entity.getCostPerClick().doubleValue() * entity.getClickCount()
                : 0.0;
        double roi = totalCpc > 0
                ? (entity.getRevenue() != null ? entity.getRevenue().doubleValue() : 0) / totalCpc
                : 0.0;
        dto.setRoi(Math.round(roi * 100.0) / 100.0);

        // Computed: durationDays = endDate - startDate + 1
        dto.setDurationDays(entity.getStartDate() != null && entity.getEndDate() != null
                ? ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1
                : 0);

        // Computed: performanceScore = 0.4*ctr + 0.3*cvr + 0.3*roi
        dto.setPerformanceScore(Math.round((0.4 * dto.getCtr() + 0.3 * dto.getCvr() + 0.3 * dto.getRoi()) * 100.0) / 100.0);
    }
}
