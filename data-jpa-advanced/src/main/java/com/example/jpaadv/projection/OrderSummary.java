package com.example.jpaadv.projection;

import java.math.BigDecimal;

/**
 * Closed (class-based) DTO projection for Order.
 *
 * Spring Data JPA can select a constructor expression directly from a JPQL query:
 *   SELECT new com.example.jpaadv.projection.OrderSummary(o.id, o.orderNo, o.status, SIZE(o.items))
 *   FROM Order o
 *
 * This avoids loading the full entity graph — only the projected fields are fetched.
 */
public class OrderSummary {

    private final Long orderId;
    private final String orderNo;
    private final String status;
    private final int itemCount;
    private final BigDecimal totalAmount;

    // Constructor used by JPQL NEW expression
    public OrderSummary(Long orderId, String orderNo, String status, Long itemCount) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.status = status;
        this.itemCount = itemCount != null ? itemCount.intValue() : 0;
        this.totalAmount = null;
    }

    public OrderSummary(Long orderId, String orderNo, String status, Long itemCount, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.status = status;
        this.itemCount = itemCount != null ? itemCount.intValue() : 0;
        this.totalAmount = totalAmount;
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNo() { return orderNo; }
    public String getStatus() { return status; }
    public int getItemCount() { return itemCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        return "OrderSummary{id=%d, orderNo='%s', status='%s', items=%d, total=%s}"
                .formatted(orderId, orderNo, status, itemCount, totalAmount);
    }
}
