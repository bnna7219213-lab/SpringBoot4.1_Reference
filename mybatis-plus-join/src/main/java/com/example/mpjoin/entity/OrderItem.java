package com.example.mpjoin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Order Item entity (junction between Order and Product).
 */
@Data
@Accessors(chain = true)
@TableName("t_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Item ID - primary key */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Associated order ID (foreign key) */
    private Long orderId;

    /** Associated product ID (foreign key) */
    private Long productId;

    /** Quantity ordered */
    private Integer quantity;

    /** Unit price at time of order */
    private BigDecimal unitPrice;

    /** Subtotal (quantity * unitPrice) */
    private BigDecimal subtotal;
}
