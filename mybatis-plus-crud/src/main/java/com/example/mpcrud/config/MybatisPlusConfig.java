package com.example.mpcrud.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus Configuration
 *
 * <p>Configures:
 * <ul>
 *   <li>MapperScan for mapper interface scanning</li>
 *   <li>PaginationInnerInterceptor for pagination support</li>
 *   <li>MetaObjectHandler for auto-filling audit fields</li>
 * </ul>
 */
@Slf4j
@Configuration
@MapperScan("com.example.mpcrud.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus interceptor with pagination support.
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // Pagination interceptor - H2 database
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.H2);
        // Prevent overflow beyond total pages
        paginationInterceptor.setOverflow(false);
        // Set max limit per query (0 = unlimited, but we set a safe default)
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    /**
     * Auto-fill handler for audit fields.
     * Automatically sets createTime and updateTime.
     */
    @Slf4j
    @Component
    public static class AuditMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            log.debug("Auto-filling audit fields on INSERT");
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            log.debug("Auto-filling audit fields on UPDATE");
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
