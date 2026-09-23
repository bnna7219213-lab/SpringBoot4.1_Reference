package com.example.marketing.service;

import com.example.marketing.dto.ActivityDTO;
import com.example.marketing.dto.CampaignDTO;
import com.example.marketing.entity.ActivityEntity;
import com.example.marketing.entity.CampaignEntity;
import com.example.marketing.mapper.ActivityMapper;
import com.example.marketing.mapper.CampaignMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketingService {

    private final CampaignMapper campaignMapper;
    private final ActivityMapper activityMapper;
    private final List<CampaignEntity> campaignStore = new ArrayList<>();
    private final List<ActivityEntity> activityStore = new ArrayList<>();

    public MarketingService(CampaignMapper campaignMapper, ActivityMapper activityMapper) {
        this.campaignMapper = campaignMapper;
        this.activityMapper = activityMapper;
        seed();
    }

    private void seed() {
        campaignStore.add(CampaignEntity.builder()
                .id(5001L).campaignName("Spring Sale 2024").campaignCode("CAMP-SPRING-2024")
                .typeId(1).budget(new BigDecimal("20000")).actualSpend(new BigDecimal("12340"))
                .startTime(LocalDateTime.of(2024, 1, 1, 0, 0)).endTime(LocalDateTime.of(2024, 1, 31, 23, 59))
                .platforms("app,web,mini")
                .targetAudienceJson("{\"age\":\"18-35\",\"gender\":\"all\"}")
                .status(2).organizerId(100L).conversionRate(new BigDecimal("0.0345"))
                .build());

        activityStore.add(ActivityEntity.builder()
                .id(6001L).activityCode("ACT-A1").campaignId(5001L).variant("treatment_b")
                .participantCount(15000).clickCount(2400).conversionCount(180)
                .revenue(new BigDecimal("45600")).costPerClick(new BigDecimal("1.25"))
                .startDate(LocalDate.of(2024, 1, 5)).endDate(LocalDate.of(2024, 1, 20))
                .build());
    }

    public List<CampaignDTO> getAllCampaigns() {
        return campaignStore.stream().map(campaignMapper::toCampaignDTO).toList();
    }

    public List<ActivityDTO> getAllActivities() {
        return activityStore.stream().map(activityMapper::toActivityDTO).toList();
    }
}
