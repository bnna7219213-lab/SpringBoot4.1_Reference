package com.example.procurement.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Mapper(componentModel = "spring")
public final class ProcurementConverters {

    /**
     * CONVERTER 1: unifiedSocialCode (18-char) -> masked first 4 + **** + last 4
     * "91440300MA5G8XXXXX" -> "9144****XXXXX"
     * BeanUtils: SILENTLY NULL — no field match + masking required
     */
    @Named("maskTaxId")
    public static String maskTaxId(String unifiedSocialCode) {
        if (unifiedSocialCode == null || unifiedSocialCode.length() < 8) return "****";
        return unifiedSocialCode.substring(0, 4) + "****" + unifiedSocialCode.substring(unifiedSocialCode.length() - 4);
    }

    /**
     * CONVERTER 2: bankName + bankAccount -> merged display
     * "China Merchants Bank" + "7559****1234" -> "招商银行 ****1234"
     * BeanUtils: SILENTLY NULL — multi-field merge
     */
    @Named("mergeBankInfo")
    public static String mergeBankInfo(String bankName, String bankAccount) {
        String maskedAcct = bankAccount != null && bankAccount.length() >= 4
                ? "****" + bankAccount.substring(bankAccount.length() - 4)
                : "****";
        return (bankName != null ? bankName : "未知银行") + " " + maskedAcct;
    }

    /**
     * CONVERTER 3: Integer cooperationStatus -> Chinese text
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("statusToText")
    public static String statusToText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未合作";
            case 1 -> "合作中";
            case 2 -> "暂停合作";
            case 3 -> "已拉黑";
            default -> "未知";
        };
    }

    /**
     * CONVERTER 4: BigDecimal score -> "92.5 / 100" string
     * BeanUtils: SILENTLY 0 (default) — BigDecimal -> String
     */
    @Named("scoreToDisplay")
    public static String scoreToDisplay(BigDecimal score) {
        if (score == null) return "0 / 100";
        return score.setScale(1, RoundingMode.HALF_UP).toPlainString() + " / 100";
    }

    /**
     * CONVERTER 5: (qualificationLevel, cooperationStatus) -> boolean isQualified
     * BeanUtils: SILENTLY false — no single field maps to boolean
     */
    @Named("computeIsQualified")
    public static boolean computeIsQualified(String qualificationLevel, Integer cooperationStatus) {
        return !"D".equals(qualificationLevel) && cooperationStatus != null && cooperationStatus == 1;
    }

    /**
     * CONVERTER 6: LocalDate establishedDate -> years since establishment
     * BeanUtils: SILENTLY 0 (default) — no direct field mapping
     */
    @Named("dateToYearsSince")
    public static long dateToYearsSince(LocalDate establishedDate) {
        if (establishedDate == null) return 0;
        return ChronoUnit.YEARS.between(establishedDate, LocalDate.now());
    }

    /**
     * CONVERTER 7: Integer urgencyLevel -> Chinese text
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("urgencyToText")
    public static String urgencyToText(Integer urgencyLevel) {
        if (urgencyLevel == null) return "普通";
        return switch (urgencyLevel) {
            case 0 -> "普通";
            case 1 -> "加急";
            case 2 -> "特急";
            case 3 -> "闪购";
            default -> "普通";
        };
    }

    /**
     * CONVERTER 8: BigDecimal budget -> formatted currency with thousand separators
     * BeanUtils: SILENTLY NULL — BigDecimal -> String
     */
    @Named("budgetToFormatted")
    public static String budgetToFormatted(BigDecimal budget) {
        if (budget == null) return "¥0.00";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);
        return "¥" + nf.format(budget);
    }

    /**
     * CONVERTER 9: approvedBy + approvedAt -> merged string
     * BeanUtils: SILENTLY NULL — multi-field merge
     */
    @Named("mergeApprovedByAt")
    public static String mergeApprovedByAt(String approvedBy, LocalDateTime approvedAt) {
        if (approvedBy == null) return "未审批";
        String time = approvedAt != null
                ? approvedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "未知时间";
        return approvedBy + " @ " + time;
    }

    /**
     * CONVERTER 10: expectedDeliveryDate -> days remaining
     * BeanUtils: SILENTLY 0 (default) — no direct field mapping
     */
    @Named("dateToDaysRemaining")
    public static long dateToDaysRemaining(LocalDate expectedDate) {
        if (expectedDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), expectedDate);
    }

    /**
     * CONVERTER 11: urgency + daysRemaining -> compliance indicator text
     * BeanUtils: SILENTLY NULL — requires multi-field computation
     */
    @Named("computeCompliance")
    public static String computeCompliance(Integer urgency, long daysRemaining) {
        if (daysRemaining < 0) return "OVERDUE";
        if (urgency != null && urgency >= 2 && daysRemaining < 3) return "CRITICAL";
        if (daysRemaining < 3) return "URGENT";
        if (urgency != null && urgency >= 1) return "ELEVATED";
        return "NORMAL";
    }
}
