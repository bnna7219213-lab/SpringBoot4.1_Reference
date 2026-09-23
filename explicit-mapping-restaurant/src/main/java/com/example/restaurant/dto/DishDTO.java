package com.example.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDTO {

    private Long id;

    // categoryId -> category name + emoji prefix
    private String displayName;

    // BigDecimal cents -> "formatPrice" string
    private String displayPrice;

    // Computed from price -> "cheap"/"moderate"/"premium"
    private String priceLevel;

    // Integer isSpicy -> Chinese spice level
    private String spiceLevel;

    // CSV string -> List<String>
    private List<String> allergenTags;

    // Computed: stock > 0 && not past closing (after 21:30)
    private boolean available;

    // JSON string -> display string
    private String specsDisplay;

    // Computed: (price - cost) / price * 100
    private double profitMargin;

    // imageUrl -> thumbnail version
    private String thumbnailUrl;
}
