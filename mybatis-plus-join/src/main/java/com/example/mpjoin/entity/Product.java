package com.example.mpjoin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Product entity (from product catalog).
 */
@Data
@Accessors(chain = true)
@TableName("t_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Product ID - primary key */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Product name */
    private String productName;

    /** Product category */
    private String category;

    /** Unit price */
    private BigDecimal price;

    /** Remaining stock */
    private Integer stock;

    /** Product status: 1=online, 0=offline */
    private Integer status;
}
