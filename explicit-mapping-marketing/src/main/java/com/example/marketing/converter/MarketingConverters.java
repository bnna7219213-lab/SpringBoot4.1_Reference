package com.example.marketing.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public final class MarketingConverters {

    private static final Map<Integer, String> CAMPAIGN_TYPES = Map.of(
            1, "优惠券", 2, "满减", 3, "拼团", 4, "秒杀"
    );

    /**
     * CONVERTER 1: Integer typeId -> typeText Chinese
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("typeIdToText")
    public static String typeIdToText(Integer typeId) {
        if (typeId == null) return "未知";
        return CAMPAIGN_TYPES.getOrDefault(typeId, "活动#" + typeId);
    }

    /**
     * CONVERTER 2: Integer status -> statusText Chinese
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("statusToText")
    public static String statusToText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审核中";
            case 2 -> "进行中";
            case 3 -> "已结束";
            case 4 -> "已下线";
            default -> "未知";
        };
    }

    /**
     * CONVERTER 3: BigDecimal -> formatted yuan display "¥12,345"
     * BeanUtils: SILENTLY NULL — BigDecimal -> String
     */
    @Named("bigDecimalToYuanDisplay")
    public static String bigDecimalToYuanDisplay(BigDecimal amount) {
        if (amount == null) return "¥0";
        return "¥" + String.format("%,.0f", amount);
    }

    /**
     * CONVERTER 4: actualSpend/budget -> "61.7%" percentage string
     * BeanUtils: SILENTLY NULL — computation from two fields
     */
    @Named("computeSpendPercent")
    public static String computeSpendPercent(BigDecimal actualSpend, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) return "0%";
        if (actualSpend == null) return "0%";
        double pct = actualSpend.divide(budget, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
        return String.format("%.1f%%", pct);
    }

    /**
     * CONVERTER 5: startTime+endTime -> Chinese date range "2024年1月1日 ~ 2024年1月31日"
     * BeanUtils: SILENTLY NULL — multi-field merge + format
     */
    @Named("dateRangeToDisplay")
    public static String dateRangeToDisplay(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return "N/A";
        String s = start != null
                ? "%d年%d月%d日".formatted(start.getYear(), start.getMonthValue(), start.getDayOfMonth())
                : "?";
        String e = end != null
                ? "%d年%d月%d日".formatted(end.getYear(), end.getMonthValue(), end.getDayOfMonth())
                : "?";
        return s + " ~ " + e;
    }

    /**
     * CONVERTER 6: CSV platforms -> display list "app(应用), web(网页), mini(小程序)"
     * BeanUtils: SILENTLY NULL — CSV -> structured display
     */
    @Named("csvToPlatformsDisplay")
    public static String csvToPlatformsDisplay(String csv) {
        if (csv == null || csv.isBlank()) return "无平台";
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(platform -> switch (platform) {
                    case "app" -> "APP(应用)";
                    case "web" -> "Web(网页)";
                    case "mini" -> "Mini(小程序)";
                    case "sms" -> "SMS(短信)";
                    default -> platform;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("无平台");
    }

    /**
     * CONVERTER 7: BigDecimal conversionRate -> "3.45%" format string
     * BeanUtils: SILENTLY NULL — BigDecimal -> formatted String
     */
    @Named("conversionRateToDisplay")
    public static String conversionRateToDisplay(BigDecimal rate) {
        if (rate == null) return "0%";
        return String.format("%.2f%%", rate.multiply(BigDecimal.valueOf(100)).doubleValue());
    }

    /**
     * CONVERTER 8: variant String -> display label "A/B Testing Variant: Treatment B"
     * BeanUtils: SILENTLY NULL — requires business rule interpretation
     */
    @Named("variantToDisplay")
    public static String variantToDisplay(String variant) {
        if (variant == null) return "Unknown Variant";
        return switch (variant) {
            case "control" -> "A/B Testing Variant: Control";
            case "treatment_a" -> "A/B Testing Variant: Treatment A";
            case "treatment_b" -> "A/B Testing Variant: Treatment B";
            default -> "Variant: " + variant;
        };
    }

    /**
     * CONVERTER 9: BigDecimal costPerClick -> "formatPrice" string
     * BeanUtils: SILENTLY NULL — BigDecimal -> String
     */
    @Named("costPerClickToDisplay")
    public static String costPerClickToDisplay(BigDecimal cpc) {
        if (cpc == null) return "¥0.00";
        return "¥" + cpc.toPlainString();
    }

    /**
     * CONVERTER 10: (ctr, cvr, roi) -> weighted performance score
     * BeanUtils: SILENTLY 0 — requires computation across computed values
     */
    @Named("computePerformanceScore")
    public static double computePerformanceScore(double ctr, double cvr, double roi) {
        return Math.round((0.4 * ctr + 0.3 * cvr + 0.3 * roi) * 100.0) / 100.0;
    }

    /**
     * CONVERTER 11: startDate+endDate -> durationDays
     * BeanUtils: SILENTLY 0 — multi-field computation
     */
    @Named("datesToDurationDays")
    public static long datesToDurationDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * CONVERTER 12: status,startTime,endTime -> isLive boolean
     * BeanUtils: SILENTLY false — requires multi-field evaluation
     */
    @Named("computeIsLive")
    public static boolean computeIsLive(Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        if (status == null || status != 2) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startTime) && !now.isAfter(endTime);
    }

    /**
     * CONVERTER 13: BigDecimal revenue -> "formatPrice" string
     * BeanUtils: SILENTLY NULL — BigDecimal -> formatted String
     */
    @Named("revenueToDisplay")
    public static String revenueToDisplay(BigDecimal revenue) {
        return bigDecimalToYuanDisplay(revenue);
    }
}
