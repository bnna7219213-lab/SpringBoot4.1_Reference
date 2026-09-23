package com.example.mpcrud.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mpcrud.entity.User;
import com.example.mpcrud.service.UserService;
import com.example.mpcrud.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * User REST Controller.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST   /api/users         - Create user</li>
 *   <li>PUT    /api/users/{id}    - Update user</li>
 *   <li>DELETE /api/users/{id}    - Delete user (logical)</li>
 *   <li>GET    /api/users/{id}    - Get user by ID</li>
 *   <li>GET    /api/users         - Paginated list with filters</li>
 *   <li>POST   /api/users/batch   - Batch create users</li>
 *   <li>GET    /api/users/search  - Fuzzy search by username</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserServiceImpl userServiceImpl;

    /**
     * Create a new user.
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Creating user: {}", user.getUsername());
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        userService.save(user);
        return user;
    }

    /**
     * Update an existing user.
     */
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("Updating user id={}", id);
        user.setId(id);
        userService.updateById(user);
        return userService.getById(id);
    }

    /**
     * Delete a user (logical delete).
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        log.info("Deleting user id={}", id);
        boolean removed = userService.removeById(id);
        return Map.of("success", removed, "id", id);
    }

    /**
     * Get a single user by ID.
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Getting user id={}", id);
        return userService.getById(id);
    }

    /**
     * Get paginated user list with optional filters.
     *
     * @param pageNum  page number (default 1)
     * @param pageSize page size (default 10)
     * @param username optional username filter
     * @param status   optional status filter
     * @param minAge   optional minimum age filter
     * @param maxAge   optional maximum age filter
     * @return paginated result
     */
    @GetMapping
    public IPage<User> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge) {

        log.info("Listing users: page={}, size={}, username={}, status={}, age=[{}, {}]",
                pageNum, pageSize, username, status, minAge, maxAge);

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // Dynamic filters using LambdaQueryWrapper (type-safe)
        wrapper.like(username != null && !username.isEmpty(), User::getUsername, username)
               .eq(status != null, User::getStatus, status)
               .ge(minAge != null, User::getAge, minAge)
               .le(maxAge != null, User::getAge, maxAge)
               .orderByDesc(User::getCreateTime);

        return userService.page(page, wrapper);
    }

    /**
     * Batch create users.
     */
    @PostMapping("/batch")
    public Map<String, Object> batchCreateUsers(@RequestBody List<User> users) {
        log.info("Batch creating {} users", users.size());
        boolean success = userServiceImpl.saveBatchUsers(users);
        return Map.of("success", success, "count", users.size());
    }

    /**
     * Fuzzy search users by username.
     */
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        log.info("Searching users with keyword: {}", keyword);
        return userService.findByUsernameFuzzy(keyword);
    }
}
