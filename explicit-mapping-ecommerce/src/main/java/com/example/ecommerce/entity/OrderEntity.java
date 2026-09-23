package com.example.ecommerce.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderEntity — simulates a JPA-loaded database entity.
 *
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │  FIELD MAPPING VISIBILITY                                                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │  id            -> OrderDTO.id              OK           (same name, type)   │
 * │  orderNo       -> OrderDTO.orderNumber     RENAME_G     (renamed field)    │
 * │  userId        -> (internal only)          —           (not in API DTO)   │
 * │  totalAmount   -> OrderDTO.totalAmount     OK           (same name, type)   │
 * │  discountAmount-> OrderDTO.discount        RENAME_D     (renamed field)    │
 * │  status        -> OrderDTO.statusText      STATUS_CODE  (0/1/2 -> Chinese) │
 * │  items         -> OrderDTO.items           TYPE_LIST    (different type)  │
 * │  shippingProvince + shippingCity + shippingDetail -> fullAddress  MERGE 3  │
 * │  createTime    -> OrderDTO.createTimeStr   TYPE_FORMAT  (DateTime->String)│
 * │  payableAmount -> (not in entity)          TOTAL_SUB    (total - discount) │
 * │  itemCountSummary -> (not in entity)       AGG_LIST     (count items+units)│
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private Integer status;     // 0=PENDING, 1=PAID, 2=CANCELLED, 3=SHIPPED, 4=COMPLETED
    private List<OrderItemEntity> items;
    private String shippingProvince;
    private String shippingCity;
    private String shippingDetail;
    private LocalDateTime createTime;
}
