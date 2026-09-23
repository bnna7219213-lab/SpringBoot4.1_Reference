package com.example.mpjoin.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.example.mpjoin.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * Product Mapper - extends MPJBaseMapper.
 */
@Mapper
public interface ProductMapper extends MPJBaseMapper<Product> {
}
