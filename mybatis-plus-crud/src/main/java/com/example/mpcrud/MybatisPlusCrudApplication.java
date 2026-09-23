package com.example.mpcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis-Plus CRUD Demo Application
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>IService/ServiceImpl for single-table CRUD</li>
 *   <li>Pagination with PaginationInnerInterceptor</li>
 *   <li>Logic delete configuration</li>
 *   <li>Auto-fill for createTime/updateTime</li>
 *   <li>Batch operations</li>
 * </ul>
 */
@SpringBootApplication
public class MybatisPlusCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusCrudApplication.class, args);
    }
}
