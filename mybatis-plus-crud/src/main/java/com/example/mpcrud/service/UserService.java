package com.example.mpcrud.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mpcrud.entity.User;

import java.util.List;

/**
 * User Service interface extending IService.
 *
 * <p>IService provides rich CRUD method out of the box:
 * <ul>
 *   <li>save / saveBatch / saveOrUpdate</li>
 *   <li>getById / getOne / list</li>
 *   <li>update / updateById / updateBatchById</li>
 *   <li>remove / removeByIds</li>
 *   <li>page / count</li>
 * </ul>
 */
public interface UserService extends IService<User> {

    /**
     * Fuzzy search users by username.
     *
     * @param keyword search keyword
     * @return list of matching users
     */
    List<User> findByUsernameFuzzy(String keyword);

    /**
     * Paginated query for users within an age range.
     *
     * @param minAge   minimum age (inclusive)
     * @param maxAge   maximum age (inclusive)
     * @param pageNum  current page number (1-based)
     * @param pageSize page size
     * @return paginated result
     */
    IPage<User> pageByAgeRange(int minAge, int maxAge, int pageNum, int pageSize);
}
