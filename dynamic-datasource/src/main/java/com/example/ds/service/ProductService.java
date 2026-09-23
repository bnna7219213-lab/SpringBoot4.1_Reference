package com.example.ds.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ds.entity.Product;

import java.util.List;

/**
 * Product Service interface demonstrating dynamic datasource switching.
 */
public interface ProductService extends IService<Product> {

    /**
     * Create a product - routes to master (write).
     *
     * @param product product to create
     * @return the created product
     */
    Product createProduct(Product product);

    /**
     * Update a product - routes to master (write).
     *
     * @param id      product id
     * @param product updated product data
     * @return the updated product
     */
    Product updateProduct(Long id, Product product);

    /**
     * Delete a product - routes to master (write).
     *
     * @param id product id
     * @return true if successful
     */
    boolean deleteProduct(Long id);

    /**
     * Get product by ID - routes to slave (read).
     *
     * @param id product id
     * @return the product
     */
    Product getProduct(Long id);

    /**
     * List all products - routes to slave (read).
     *
     * @return all products
     */
    List<Product> listAllProducts();

    /**
     * Count all products - routes to slave (read).
     *
     * @return total count
     */
    Long countAll();

    /**
     * Dynamic datasource switching using SpEL parameter.
     *
     * @param dsName datasource name ("master" or "slave")
     * @param id     product id
     * @return product from the specified datasource
     */
    Product getProductFromDs(String dsName, Long id);

    /**
     * Manually switch datasource using context holder.
     *
     * @param dsName datasource name
     * @return product using manual datasource selection
     */
    Product getProductManualSwitch(Long id);

    /**
     * Demonstrate read-write split: write master then read slave.
     * May show replication lag.
     *
     * @param product product to create and then read
     * @return the read-back product from slave
     */
    Product writeMasterReadSlave(Product product);
}
