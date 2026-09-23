package com.example.mpjoin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity (main table for join queries).
 */
@Data
@Accessors(chain = true)
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Order ID - primary key */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Order number */
    private String orderNo;

    /** Customer name */
    private String customerName;

    /** Total amount */
    private BigDecimal totalAmount;

    /** Order status: 1=pending, 2=shipped, 3=completed, 4=cancelled */
    private Integer status;

    /** Order creation time */
    private LocalDateTime createTime;
}
