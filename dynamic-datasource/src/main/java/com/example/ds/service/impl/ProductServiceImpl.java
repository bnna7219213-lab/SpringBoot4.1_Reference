package com.example.ds.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ds.entity.Product;
import com.example.ds.mapper.ProductMapper;
import com.example.ds.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Service Implementation with Dynamic DataSource Switching.
 *
 * <p>Key annotations:
 * <ul>
 *   <li>@DS("master") - routes to master database (write operations)</li>
 *   <li>@DS("slave") - routes to slave database (read operations)</li>
 *   <li>@DS("#dsName") - SpEL-based dynamic selection from method parameter</li>
 * </ul>
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    // =================== WRITE OPERATIONS (Master) ===================

    /**
     * Create product on master (write).
     * @DS("master") AOP intercepts and sets datasource to master before method execution.
     * Transaction ensures atomicity within master connection.
     */
    @Override
    @DS("master")
    @Transactional(rollbackFor = Exception.class)
    public Product createProduct(Product product) {
        log.info("[MASTER] Creating product: {}", product.getName());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        save(product);
        return product;
    }

    /**
     * Update product on master (write).
     */
    @Override
    @DS("master")
    @Transactional(rollbackFor = Exception.class)
    public Product updateProduct(Long id, Product product) {
        log.info("[MASTER] Updating product id={}", id);
        product.setId(id);
        product.setUpdateTime(LocalDateTime.now());
        updateById(product);
        // Return from master for consistency
        return getById(id);
    }

    /**
     * Delete product on master (write).
     */
    @Override
    @DS("master")
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long id) {
        log.info("[MASTER] Deleting product id={}", id);
        return removeById(id);
    }

    // =================== READ OPERATIONS (Slave) ===================

    /**
     * Get product from slave (read).
     * Read operations go to slave to reduce load on master.
     */
    @Override
    @DS("slave")
    public Product getProduct(Long id) {
        log.info("[SLAVE] Reading product id={}", id);
        return getById(id);
    }

    /**
     * List all products from slave (read).
     */
    @Override
    @DS("slave")
    public List<Product> listAllProducts() {
        log.info("[SLAVE] Listing all products");
        return list();
    }

    /**
     * Count products from slave (read).
     */
    @Override
    @DS("slave")
    public Long countAll() {
        log.info("[SLAVE] Counting all products");
        return baseMapper.countAll();
    }

    // =================== DYNAMIC SWITCHING ===================

    /**
     * Dynamic datasource switching using SpEL.
     * The @DS("#dsName") expression reads the 'dsName' method parameter
     * and uses it to select the datasource at runtime.
     *
     * This is useful when datasource name is determined by:
     * - User request parameter
     * - Configuration
     * - Business logic
     */
    @Override
    @DS("#dsName")
    public Product getProductFromDs(String dsName, Long id) {
        log.info("[DYNAMIC - {}] Reading product id={}", dsName, id);
        return getById(id);
    }

    /**
     * Manual datasource switching using DynamicDataSourceContextHolder.
     *
     * <p>Use this approach when:
     * <ul>
     *   <li>You need to switch datasource inside method body</li>
     *   <li>@DS annotation is not granular enough</li>
     *   <li>You need to push/pop datasources</li>
     * </ul>
     */
    @Override
    public Product getProductManualSwitch(Long id) {
        try {
            // Push slave to context - all DB operations in this scope go to slave
            DynamicDataSourceContextHolder.push("slave");
            log.info("[MANUAL PUSH] Reading product id={} from slave", id);
            Product product = getById(id);

            // Demonstrate switching to master within same method
            DynamicDataSourceContextHolder.push("master");
            log.info("[MANUAL PUSH] Re-reading product id={} from master", id);
            Product fromMaster = getById(id);

            // Return the version from whichever datasource you need
            log.info("[MANUAL PUSH] Returning combined result");
            return product;
        } finally {
            // IMPORTANT: Always pop in finally to clean up thread-local
            DynamicDataSourceContextHolder.poll();
            log.info("[MANUAL POLL] Cleaned up datasource context");
        }
    }

    // =================== READ-WRITE SPLIT PATTERN ===================

    /**
     * Demonstrates read-write split:
     * 1. Write to master
     * 2. Read from slave (may show replication lag in real MySQL)
     *
     * <p>In this demo with H2, both databases have the same initial data,
     * so the write will be visible on slave because we're simulating replication.
     */
    @Override
    public Product writeMasterReadSlave(Product product) {
        // Step 1: Write to master (no @DS, delegates to createProduct which has @DS("master"))
        log.info("[WRITE-READ] Writing to master: {}", product.getName());
        Product created = createProduct(product);

        // Step 2: Read from slave (may show replication lag in production)
        log.info("[WRITE-READ] Reading back from slave: id={}", created.getId());
        return getProduct(created.getId());
    }
}
