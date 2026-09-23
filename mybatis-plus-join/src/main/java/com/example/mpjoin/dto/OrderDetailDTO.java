package com.example.mpjoin.dto;

import com.example.mpjoin.entity.Order;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Order Detail DTO - combines Order with nested items and product info.
 *
 * <p>This DTO is populated using MPJLambdaJoinWrapper's join capabilities
 * to flatten multi-table JOIN results into a single structure.
 */
@Data
public class OrderDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== Order fields (abbreviated) =====

    /** Order ID */
    private Long orderId;

    /** Order number */
    private String orderNo;

    /** Customer name */
    private String customerName;

    /** Total amount */
    private java.math.BigDecimal totalAmount;

    /** Order status */
    private Integer status;

    /** Order creation time */
    private java.time.LocalDateTime createTime;

    // ===== Nested order items =====

    /** List of items in this order */
    private List<OrderItemDetail> items;

    /**
     * Nested item detail within an order.
     */
    @Data
    public static class OrderItemDetail implements Serializable {

        private static final long serialVersionUID = 1L;

        /** Item ID */
        private Long itemId;

        /** Quantity */
        private Integer quantity;

        /** Unit price */
        private java.math.BigDecimal unitPrice;

        /** Subtotal */
        private java.math.BigDecimal subtotal;

        // ===== Product info (from JOIN) =====

        /** Product ID */
        private Long productId;

        /** Product name */
        private String productName;

        /** Product category */
        private String category;

        /** Current product price */
        private java.math.BigDecimal currentPrice;
    }
}
