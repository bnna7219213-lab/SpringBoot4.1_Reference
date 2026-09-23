package com.example.restaurant.mapper;

import com.example.restaurant.converter.RestaurantConverters;
import com.example.restaurant.dto.DishDTO;
import com.example.restaurant.entity.DishEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = RestaurantConverters.class)
public interface DishMapper {

    @Mapping(source = "categoryId", target = "displayName", qualifiedByName = "categoryIdToDisplayName")
    @Mapping(source = "price", target = "displayPrice", qualifiedByName = "centsToDisplayPrice")
    @Mapping(source = "price", target = "priceLevel", qualifiedByName = "priceToLevel")
    @Mapping(source = "isSpicy", target = "spiceLevel", qualifiedByName = "spiceLevelToChinese")
    @Mapping(source = "allergens", target = "allergenTags", qualifiedByName = "csvToList")
    @Mapping(source = "stock", target = "available", qualifiedByName = "stockToAvailable")
    @Mapping(source = "dishName", target = "thumbnailUrl")
    @Mapping(target = "specsDisplay", ignore = true)
    @Mapping(target = "profitMargin", ignore = true)
    DishDTO toDishDTO(DishEntity entity);

    @AfterMapping
    default void fillComputedFields(DishEntity entity, @MappingTarget DishDTO dto) {
        // Compute: profitMargin = (price-cost)/price * 100
        if (entity.getPrice() != null) {
            BigDecimal cost = entity.getCost() != null ? entity.getCost() : BigDecimal.ZERO;
            double margin = entity.getPrice().subtract(cost)
                    .divide(entity.getPrice(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            dto.setProfitMargin(Math.round(margin * 100.0) / 100.0);
        }

        // Pre-process specs JSON to display string
        if (entity.getSpecs() != null && !entity.getSpecs().isBlank()) {
            dto.setSpecsDisplay(entity.getSpecs().replaceAll("[{}\"]", ""));
        }
    }

    @Named("dishNameToThumbnail")
    default String dishNameToThumbnail(String dishName) {
        if (dishName == null) return "https://cdn.example.com/dishes/default.jpg";
        String slug = dishName.toLowerCase().replace(" ", "-");
        return "https://cdn.example.com/dishes/" + slug + ".jpg";
    }
}
