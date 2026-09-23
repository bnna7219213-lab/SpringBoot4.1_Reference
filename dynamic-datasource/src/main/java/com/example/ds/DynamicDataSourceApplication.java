package com.example.ds;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Dynamic DataSource Demo Application
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>Multiple data sources (master + slave) configuration</li>
 *   <li>@DS annotation for explicit datasource switching</li>
 *   <li>Read-write splitting with AOP</li>
 *   <li>SpEL expression-based datasource selection</li>
 *   <li>DynamicDataSourceContextHolder manual push/pop</li>
 * </ul>
 *
 * <p>Architecture:
 * <pre>
 *   [Controller]
 *       |
 *   [Service] --- read --> @DS("slave") --- H2 Slave DB
 *       |
 *   [Service] --- write -> @DS("master") --- H2 Master DB
 * </pre>
 */
@EnableTransactionManagement
@MapperScan("com.example.ds.mapper")
@SpringBootApplication
public class DynamicDataSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicDataSourceApplication.class, args);
    }
}
