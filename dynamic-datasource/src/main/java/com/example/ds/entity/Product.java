package com.example.ds.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity - identical schema on both master and slave databases.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key - auto increment */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Product name */
    private String name;

    /** Product category */
    private String category;

    /** Unit price */
    private BigDecimal price;

    /** Stock quantity */
    private Integer stock;

    /** Product status: 1=online, 0=offline */
    private Integer status;

    /** Creation time */
    private LocalDateTime createTime;

    /** Last update time */
    private LocalDateTime updateTime;
}
