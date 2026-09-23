package com.example.mpcrud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mpcrud.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * User Mapper - extends BaseMapper to inherit CRUD methods.
 *
 * <p>BaseMapper provides:
 * <ul>
 *   <li>insert(T entity)</li>
 *   <li>deleteById(Serializable id)</li>
 *   <li>updateById(T entity)</li>
 *   <li>selectById(Serializable id)</li>
 *   <li>selectList(Wrapper&lt;T&gt; queryWrapper)</li>
 *   <li>selectPage(...) - pagination</li>
 *   <li>and more...</li>
 * </ul>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * Custom SQL query: fuzzy search by username.
     * Demonstrates writing custom SQL when BaseMapper methods are insufficient.
     *
     * @param keyword search keyword
     * @return list of matching users
     */
    @Select("SELECT * FROM t_user WHERE username LIKE CONCAT('%', #{keyword}, '%') AND deleted = 0")
    List<User> findByUsernameFuzzy(@Param("keyword") String keyword);
}
