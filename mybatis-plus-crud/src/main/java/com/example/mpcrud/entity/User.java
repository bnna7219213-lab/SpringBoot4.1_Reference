package com.example.mpcrud.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity demonstrating MyBatis-Plus annotations.
 *
 * <ul>
 *   <li>@TableName - maps to database table</li>
 *   <li>@TableId(type = AUTO) - auto-increment primary key</li>
 *   <li>@TableField(fill = INSERT) - auto-fill on insert</li>
 *   <li>@EnumValue - maps enum field to DB value</li>
 *   <li>@TableLogic - enables logical delete</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key - auto increment
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Username
     */
    private String username;

    /**
     * Email address
     */
    private String email;

    /**
     * Age
     */
    private Integer age;

    /**
     * Status: 1=active, 0=inactive
     * Uses @EnumValue to map enum value to database integer
     */
    @EnumValue
    private Integer status;

    /**
     * Account creation time - auto-filled on INSERT
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Last update time - auto-filled on INSERT and UPDATE
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Logical delete flag: 0=not deleted, 1=deleted
     * This field will be auto-handled by MyBatis-Plus
     */
    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}
