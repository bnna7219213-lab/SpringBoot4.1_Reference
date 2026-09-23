package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order API response DTO.
 *
 * 12 target fields — only 2 can be mapped correctly by BeanUtils (id, totalAmount).
 * 10 require explicit configuration: rename, type conversion, formatting, computation, merging.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;

    /**
     * MISMATCH TYPE: Renamed field
     * Entity: orderNo     DTO: orderNumber
     * BeanUtils: null
     */
    private String orderNumber;

    private BigDecimal totalAmount;

    /**
     * MISMATCH TYPE: Renamed field
     * Entity: discountAmount    DTO: discount
     * BeanUtils: null
     */
    private BigDecimal discount;

    /**
     * MISMATCH TYPE: Type + Semantic change (Integer -> String)
     * Entity.status: 0=PENDING 1=PAID 2=CANCELLED
     * DTO.statusText: "待支付"/"已支付"/"已取消"
     * BeanUtils: null
     */
    private String statusText;

    /**
     * MISMATCH TYPE: Computed field
     * Entity does NOT have this field. Computed as: totalAmount - discountAmount
     * BeanUtils: null
     */
    private BigDecimal payableAmount;

    /**
     * MISMATCH TYPE: 3-field merge
     * Entity: shippingProvince + shippingCity + shippingDetail
     * DTO: single fullAddress string
     * BeanUtils: null
     */
    private String fullAddress;

    /**
     * MISMATCH TYPE: Type conversion (List<OrderItemEntity> -> List<OrderItemDTO>)
     * Each item also needs unitPrice * quantity -> subtotal computation
     * BeanUtils: null (type mismatch)
     */
    private List<OrderItemDTO> items;

    /**
     * MISMATCH TYPE: Type conversion + formatting
     * Entity: createTime (LocalDateTime)    DTO: createTimeStr ("2024-01-15 14:30")
     * BeanUtils: null
     */
    private String createTimeStr;

    /**
     * MISMATCH TYPE: Multi-field aggregate from list
     * Computed as: "3 items, 12 units total" from items list
     * BeanUtils: null
     */
    private String itemCountSummary;
}
