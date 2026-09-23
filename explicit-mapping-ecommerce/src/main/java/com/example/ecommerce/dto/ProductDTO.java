package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product API response DTO.
 *
 * Comparison with ProductEntity:
 * - name:             direct mapping (OK)
 * - sku:              direct mapping (OK)
 * - unitPrice (BD):   -> formattedPrice (String)   Type+format conversion (FAIL with BeanUtils)
 * - stock:            -> inStock (boolean)          Computed (FAIL with BeanUtils)
 * - weight (BD):      -> weightDisplay (String)     Type+format conversion (FAIL with BeanUtils)
 * - categoryCode:     -> categoryName (String)      Business lookup (FAIL with BeanUtils)
 * - status, createTime: excluded from DTO           Not exposed to API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String sku;

    /**
     * REQUIRES: BigDecimal -> "formatPrice" string
     * BeanUtils: SILENTLY NULL (type mismatch)
     */
    private String formattedPrice;

    /**
     * REQUIRES: stock > 0 -> boolean inStock
     * BeanUtils: SILENTLY false (no type compatibility)
     */
    private boolean inStock;

    /**
     * REQUIRES: categoryCode "ELEC-ACC" -> "Electronic Accessories"
     * BeanUtils: SILENTLY NULL (business mapping)
     */
    private String categoryName;

    /**
     * REQUIRES: weight in kg -> "85g" or "1.2kg" display string
     * BeanUtils: SILENTLY NULL (type + formatting)
     */
    private String weightDisplay;
}
