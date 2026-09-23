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
public class FoodOrderEntity {

    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String itemsJson;        // JSON string of items
    private BigDecimal totalAmount;   // cents
    private Integer tableNumber;
    private LocalDateTime bookingTime;
    private String remark;
    private String status;           // CREATED/PREPARING/READY/SERVED/CANCELLED
    private LocalDateTime createdAt;
}
