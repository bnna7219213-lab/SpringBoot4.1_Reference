package com.example.ecommerce.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * E-commerce specific type converters used by MapStruct mappers.
 *
 * Each @Named method here maps a specific incompatible type conversion that
 * BeanUtils CANNOT handle. BeanUtils only handles source-type == dest-type
 * with identical field names. Everything below silently fails with BeanUtils.
 */
@Mapper(componentModel = "spring")
public final class EcommerceConverters {

    // Category code-to-name lookup table (in production this would be from DB/cache)
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "ELEC-ACC", "Electronic Accessories",
            "ELEC-PHONE", "Smartphones",
            "CLOTH-MEN", "Men's Clothing",
            "CLOTH-WOMEN", "Women's Clothing",
            "FOOD-BEVERAGE", "Food & Beverages",
            "HOME-FURNITURE", "Home & Furniture"
    );

    /**
     * CONVERTER 1: Integer status code -> Chinese status text
     *
     * Mismatch type: Type + Semantic change
     * BeanUtils failure: Integer -> String is a different type, BeanUtils silently skips
     */
    @Named("statusToText")
    public static String statusToText(Integer status) {
        if (status == null) return "未知状态";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已取消";
            case 3 -> "已发货";
            case 4 -> "已完成";
            default -> "未知状态";
        };
    }

    /**
     * CONVERTER 2: BigDecimal price -> formatted currency string "formatPrice"
     *
     * Mismatch type: Formatting (BigDecimal -> String with currency symbol + 2 decimal)
     * BeanUtils failure: BigDecimal cannot be directly assigned to String — silently null
     */
    @Named("priceToFormatted")
    public static String priceToFormatted(BigDecimal price) {
        if (price == null) return "¥0.00";
        return "¥" + price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * CONVERTER 3: Category code -> category name (business lookup)
     *
     * Mismatch type: Business semantic conversion
     * BeanUtils failure: Different field name + requires business logic — silently null
     */
    @Named("categoryCodeToName")
    public static String categoryCodeToName(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) return "未分类";
        return CATEGORY_NAMES.getOrDefault(categoryCode, "分类:" + categoryCode);
    }

    /**
     * CONVERTER 4: BigDecimal weight (kg) -> display string
     * < 1kg -> "85g" or "500g"; >= 1kg -> "1.2kg"
     *
     * Mismatch type: Type conversion + formatting + business rule
     * BeanUtils failure: BigDecimal -> String, with unit conversion — silently null
     */
    @Named("weightToDisplay")
    public static String weightToDisplay(BigDecimal weightKg) {
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) return "—";
        if (weightKg.compareTo(BigDecimal.ONE) < 0) {
            // Convert to grams
            int grams = weightKg.multiply(BigDecimal.valueOf(1000)).intValue();
            return grams + "g";
        }
        return weightKg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "kg";
    }

    /**
     * CONVERTER 5: LocalDateTime -> formatted string
     *
     * Mismatch type: Type + formatting
     * BeanUtils failure: LocalDateTime -> String is a different type — silently null
     */
    @Named("dateTimeToFormatted")
    public static String dateTimeToFormatted(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * CONVERTER 6: Integer stock -> boolean inStock
     *
     * Mismatch type: Type + semantic change (Integer -> boolean)
     * BeanUtils failure: Integer cannot be assigned to boolean — silently remains false
     */
    @Named("stockToInStock")
    public static boolean stockToInStock(Integer stock) {
        return stock != null && stock > 0;
    }
}
