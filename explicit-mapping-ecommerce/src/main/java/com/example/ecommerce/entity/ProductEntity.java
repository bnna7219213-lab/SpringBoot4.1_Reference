package com.example.ecommerce.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductEntity — simulates a JPA-loaded database entity.
 *
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │  FIELD MAPPING VISIBILITY: What BeanUtils can and cannot map correctly       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │  name          -> ProductDTO.name          RENAME_OK    (same name, type)   │
 * │  sku           -> ProductDTO.sku           OK           (same name, type)   │
 * │  unitPrice     -> ProductDTO.formattedPrice TYPE_CONV_B  (BigDecimal->String)│
 * │  stock         -> ProductDTO.inStock       COMPUTED     (stock>0 boolean)  │
 * │  weight        -> ProductDTO.weightDisplay TYPE_CONV_K  (BigDecimal->String)│
 * │  categoryCode  -> ProductDTO.categoryName  LOOKUP       (code -> name map)  │
 * │  status        -> (ignored)                —           (not in API DTO)   │
 * │  createTime    -> (ignored)                —           (not in API DTO)   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal unitPrice;
    private Integer stock;
    private BigDecimal weight;       // in kilograms
    private String categoryCode;     // e.g., "ELEC-ACC"
    private Integer status;          // 0=INACTIVE, 1=ACTIVE, 2=DISCONTINUED
    private LocalDateTime createTime;
}
