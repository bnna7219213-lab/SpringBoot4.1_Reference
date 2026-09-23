package com.example.mpjoin.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.example.mpjoin.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * Order Mapper - extends MPJBaseMapper (from mybatis-plus-join).
 *
 * <p>MPJBaseMapper extends MyBatis-Plus BaseMapper and adds JOIN capabilities:
 * <ul>
 *   <li>selectJoinList(Class&lt;T&gt; clazz, MPJLambdaJoinWrapper wrapper)</li>
 *   <li>selectJoinPage(Class&lt;T&gt; clazz, IPage page, MPJLambdaJoinWrapper wrapper)</li>
 *   <li>selectJoinOne(Class&lt;T&gt; clazz, MPJLambdaJoinWrapper wrapper)</li>
 * </ul>
 *
 * <p>Use MPJLambdaJoinWrapper for type-safe JOIN queries.
 */
@Mapper
public interface OrderMapper extends MPJBaseMapper<Order> {
}
