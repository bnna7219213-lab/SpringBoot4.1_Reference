package com.example.restaurant.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public final class RestaurantConverters {

    private static final Map<Long, String Category_NAMES = Map.of(
            1L, "Appetizer", 2L, "Main Course", 3L, "Sichuan", 4L, "Dessert", 5L, "Drink"
    );

    private static final Map<Long, String> CATEGORY_EMOJI = Map.of(
            1L, "\uD83C\uDF5F", 2L, "\uD83C\uDF5C", 3L, "\uD83C\uDF36\uFE0F", 4L, "\uD83C\uDF70", 5L, "\uD83C\uDF79"
    );

    /**
     * CONVERTER 1: price from cents Integer (1999) -> display price "formatPrice"
     * BeanUtils: SILENTLY NULL — BigDecimal -> String
     */
    @Named("centsToDisplayPrice")
    public static String centsToDisplayPrice(BigDecimal cents) {
        if (cents == null) return "¥0.00";
        BigDecimal yuan = cents.divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        return "¥" + yuan.toPlainString();
    }

    /**
     * CONVERTER 2: CSV allergens -> List<String>
     * BeanUtils: SILENTLY NULL — String -> List<String>
     */
    @Named("csvToList")
    public static List<String> csvToList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * CONVERTER 3: Integer spice level -> Chinese text
     * BeanUtils: SILENTLY NULL — Integer -> String
     */
    @Named("spiceLevelToChinese")
    public static String spiceLevelToChinese(Integer level) {
        if (level == null) return "不辣";
        return switch (level) {
            case 0 -> "不辣";
            case 1 -> "微辣";
            case 2 -> "中辣";
            case 3 -> "特辣";
            default -> "不辣";
        };
    }

    /**
     * CONVERTER 4: categoryId -> displayName with emoji
     * BeanUtils: SILENTLY NULL — no direct mapping, requires lookup + concatenation
     */
    @Named("categoryIdToDisplayName")
    public static String categoryIdToDisplayName(Long categoryId) {
        String emoji = CATEGORY_EMOJI.getOrDefault(categoryId, "\uD83C\uDF5D");
        String categoryName = CATEGORY_NAMES.getOrDefault(categoryId, "");
        return categoryName.isEmpty() ? emoji : emoji + " " + categoryName;
    }

    /**
     * CONVERTER 5: BigDecimal price -> priceLevel (cheap/moderate/premium)
     * BeanUtils: SILENTLY NULL — requires computation
     */
    @Named("priceToLevel")
    public static String priceToLevel(BigDecimal cents) {
        if (cents == null) return "cheap";
        double yuan = cents.doubleValue() / 100.0;
        if (yuan < 15.0) return "cheap";
        if (yuan < 40.0) return "moderate";
        return "premium";
    }

    /**
     * CONVERTER 6: bookingTime -> waitMinutes (computed from now)
     * BeanUtils: SILENTLY NULL — requires time computation
     */
    @Named("bookingTimeToWaitMinutes")
    public static long bookingTimeToWaitMinutes(LocalDateTime bookingTime) {
        if (bookingTime == null) return 0;
        return ChronoUnit.MINUTES.between(bookingTime, LocalDateTime.now());
    }

    /**
     * CONVERTER 7: Integer tableNumber -> "Table: A05"
     * BeanUtils: SILENTLY NULL — Integer -> String with formatting
     */
    @Named("tableNumberToDisplay")
    public static String tableNumberToDisplay(Integer tableNumber) {
        if (tableNumber == null) return "Table: N/A";
        return "Table: A" + String.format("%02d", tableNumber);
    }

    /**
     * CONVERTER 8: Integer stock -> boolean available (stock > 0)
     * BeanUtils: SILENTLY false — Integer -> boolean
     */
    @Named("stockToAvailable")
    public static boolean stockToAvailable(Integer stock) {
        return stock != null && stock > 0;
    }

    /**
     * CONVERTER 9: Compute profit margin: (price-cost)/price * 100
     * BeanUtils: SILENTLY 0 — requires computation
     */
    @Named("computeProfitMargin")
    public static double computeProfitMargin(BigDecimal price, BigDecimal cost) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        BigDecimal actualCost = cost != null ? cost : BigDecimal.ZERO;
        return price.subtract(actualCost)
                .divide(price, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * CONVERTER 10: LocalDateTime -> formatted string
     * BeanUtils: SILENTLY NULL — different types
     */
    @Named("dateTimeToFormattedStr")
    public static String dateTimeToFormattedStr(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
