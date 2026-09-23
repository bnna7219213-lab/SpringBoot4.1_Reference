package com.example.mpjoin.service;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Product Sales Value Object - shows product with total sales.
 *
 * <p>Result of LEFT JOIN between Product and OrderItem.
 */
@Data
public class ProductSalesVO {

    /** Product ID */
    private Long productId;

    /** Product name */
    private String productName;

    /** Product category */
    private String category;

    /** Unit price */
    private BigDecimal price;

    /** Total quantity sold across all orders */
    private Integer totalSold;

    /** Total revenue from this product */
    private BigDecimal totalRevenue;
}
