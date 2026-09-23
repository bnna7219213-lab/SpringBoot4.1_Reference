package com.example.mpcrud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mpcrud.entity.User;
import com.example.mpcrud.mapper.UserMapper;
import com.example.mpcrud.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service Implementation.
 *
 * <p>ServiceImpl&lt;UserMapper, User&gt; provides the default implementation
 * of all IService methods. Custom business logic is added here.
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public List<User> findByUsernameFuzzy(String keyword) {
        log.info("Fuzzy searching users with keyword: {}", keyword);
        // Delegate to the custom SQL defined in the mapper
        return baseMapper.findByUsernameFuzzy(keyword);
    }

    @Override
    public IPage<User> pageByAgeRange(int minAge, int maxAge, int pageNum, int pageSize) {
        log.info("Querying users by age range: {} - {}, page: {}/{}", minAge, maxAge, pageNum, pageSize);

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getAge, minAge)
               .le(User::getAge, maxAge)
               .orderByDesc(User::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    /**
     * Demonstrates batch save with custom logic.
     * Overrides the default saveBatch to add validation.
     *
     * @param userList list of users to save
     * @return true if successful
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUsers(List<User> userList) {
        log.info("Batch saving {} users", userList.size());

        // Set default status for users without status
        userList.stream()
                .filter(user -> user.getStatus() == null)
                .forEach(user -> user.setStatus(1));

        // Use IService's saveBatch with custom batch size
        return saveBatch(userList, 500);
    }
}
