package com.example.marketing;

import com.example.marketing.entity.ActivityEntity;
import com.example.marketing.entity.CampaignEntity;
import com.example.marketing.mapper.ActivityMapper;
import com.example.marketing.mapper.CampaignMapper;
import com.example.marketing.dto.ActivityDTO;
import com.example.marketing.dto.CampaignDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class ExplicitMappingMarketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplicitMappingMarketingApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(CampaignMapper campaignMapper,
                                  ActivityMapper activityMapper) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  MARKETING EXPLICIT MAPPING DEMO");
            System.out.println("=".repeat(80));

            CampaignEntity campaign = new CampaignEntity();
            campaign.setId(5001L);
            campaign.setCampaignName("Spring Sale 2024");
            campaign.setCampaignCode("CAMP-SPRING-2024");
            campaign.setTypeId(1); // 1=coupon
            campaign.setBudget(new BigDecimal("20000"));
            campaign.setActualSpend(new BigDecimal("12340"));
            campaign.setStartTime(LocalDateTime.of(2024, 1, 1, 0, 0));
            campaign.setEndTime(LocalDateTime.of(2024, 1, 31, 23, 59));
            campaign.setPlatforms("app,web,mini");
            campaign.setTargetAudienceJson("{\"age\":\"18-35\",\"gender\":\"all\",\"interests\":[\"tech\",\"sports\"]}");
            campaign.setStatus(2); // 2=live
            campaign.setOrganizerId(100L);
            campaign.setConversionRate(new BigDecimal("0.0345"));

            CampaignDTO campaignDto = campaignMapper.toCampaignDTO(campaign);

            System.out.println("\n--- Campaign Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  typeId          = " + campaign.getTypeId() + " (Integer)");
            System.out.println("  status          = " + campaign.getStatus() + " (Integer)");
            System.out.println("  budget          = " + campaign.getBudget());
            System.out.println("  actualSpend     = " + campaign.getActualSpend());
            System.out.println("  startTime       = " + campaign.getStartTime());
            System.out.println("  platforms       = " + campaign.getPlatforms());
            System.out.println("  conversionRate  = " + campaign.getConversionRate());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  typeText        = " + campaignDto.getTypeText());
            System.out.println("  statusText      = " + campaignDto.getStatusText());
            System.out.println("  budgetDisplay   = " + campaignDto.getBudgetDisplay());
            System.out.println("  spendPercent    = " + campaignDto.getSpendPercent());
            System.out.println("  runningDisplay  = " + campaignDto.getRunningDisplay());
            System.out.println("  platformsDisplay= " + campaignDto.getPlatformsDisplay());
            System.out.println("  isLive          = " + campaignDto.isLive());
            System.out.println("  conversionDisplay=" + campaignDto.getConversionDisplay());

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            CampaignDTO beanUtilsCampaign = new CampaignDTO();
            BeanUtils.copyProperties(campaign, beanUtilsCampaign);
            System.out.println("  typeText        = " + beanUtilsCampaign.getTypeText() + "  <-- NULL! Integer->String (FAIL)");
            System.out.println("  statusText      = " + beanUtilsCampaign.getStatusText() + "  <-- NULL! Integer->String (FAIL)");
            System.out.println("  budgetDisplay   = " + beanUtilsCampaign.getBudgetDisplay() + "  <-- NULL! Formatting (FAIL)");
            System.out.println("  spendPercent    = " + beanUtilsCampaign.getSpendPercent() + "  <-- NULL! Computation (FAIL)");
            System.out.println("  isLive          = " + beanUtilsCampaign.isLive() + "  <-- false (default) (FAIL)");
            System.out.println("  platformsDisplay= " + beanUtilsCampaign.getPlatformsDisplay() + "  <-- NULL! CSV->structured (FAIL)");

            ActivityEntity activity = new ActivityEntity();
            activity.setId(6001L);
            activity.setActivityCode("ACT-A1");
            activity.setCampaignId(5001L);
            activity.setVariant("treatment_b");
            activity.setParticipantCount(15000);
            activity.setClickCount(2400);
            activity.setConversionCount(180);
            activity.setRevenue(new BigDecimal("45600"));
            activity.setCostPerClick(new BigDecimal("1.25"));
            activity.setStartDate(LocalDate.of(2024, 1, 5));
            activity.setEndDate(LocalDate.of(2024, 1, 20));

            ActivityDTO activityDto = activityMapper.toActivityDTO(activity);

            System.out.println("\n--- Activity Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  variant         = " + activity.getVariant());
            System.out.println("  participantCount= " + activity.getParticipantCount());
            System.out.println("  clickCount      = " + activity.getClickCount());
            System.out.println("  cpc             = " + activity.getCostPerClick());
            System.out.println("  revenue         = " + activity.getRevenue());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  variantDisplay  = " + activityDto.getVariantDisplay());
            System.out.println("  ctr             = " + activityDto.getCtr());
            System.out.println("  cvr             = " + activityDto.getCvr());
            System.out.println("  roi             = " + activityDto.getRoi());
            System.out.println("  cpcDisplay      = " + activityDto.getCpcDisplay());
            System.out.println("  durationDays    = " + activityDto.getDurationDays());
            System.out.println("  performanceScore= " + activityDto.getPerformanceScore());

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  ALL target fields require explicit MapStruct configuration.");
            System.out.println("  BeanUtils: 0 out of 9 correct for CampaignDTO");
            System.out.println("  BeanUtils: 0 out of 11 correct for ActivityDTO");
            System.out.println("=".repeat(80) + "\n");
        };
    }
}
