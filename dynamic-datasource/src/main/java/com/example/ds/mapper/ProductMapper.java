package com.example.ds.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ds.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Product Mapper - uses BaseMapper for CRUD.
 * The actual datasource is determined by @DS annotation at service level.
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * Count products - will go to slave by default via @DS("slave").
     * Demonstrates slave-read for aggregate queries.
     *
     * @return total product count
     */
    @Select("SELECT COUNT(*) FROM t_product")
    Long countAll();

    /**
     * Find products by category - read operation (slave).
     *
     * @param category product category
     * @return matching products
     */
    @Select("SELECT * FROM t_product WHERE category = #{category}")
    List<Product> findByCategory(@Param("category") String category);
}
