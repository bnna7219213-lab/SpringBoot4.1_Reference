package com.example.mpjoin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis-Plus-Join Demo Application
 *
 * <p>Demonstrates multi-table JOIN queries using the community extension
 * mybatis-plus-join (MPJLambdaJoinWrapper) without writing SQL.
 *
 * <p>Relationship: Order 1:N OrderItem N:1 Product
 */
@SpringBootApplication
public class MybatisPlusJoinApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusJoinApplication.class, args);
    }
}
