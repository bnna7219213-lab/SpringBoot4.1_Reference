package com.example.mpjoin.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.example.mpjoin.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order Item Mapper - extends MPJBaseMapper.
 */
@Mapper
public interface OrderItemMapper extends MPJBaseMapper<OrderItem> {
}
