package com.example.restaurant.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishEntity {

    private Long id;

    // Reverted and used for display prefix
    private String dishName;
    private String dishCode;
    private Long categoryId;
    private BigDecimal price;      // INTEGER cents (e.g., 1999 = $19.99)
    private BigDecimal cost;       // cents
    private Integer stock;         // daily stock
    private Integer isSpicy;       // 0=none, 1=mild, 2=medium, 3=hot
    private String allergens;      // CSV: "peanut,shellfish"
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String specs;          // JSON string
}
