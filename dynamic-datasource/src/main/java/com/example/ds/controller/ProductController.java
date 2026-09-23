package com.example.ds.controller;

import com.example.ds.entity.Product;
import com.example.ds.service.ProductService;
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
 * Product REST Controller with dynamic datasource demonstration.
 *
 * <p>Endpoints show how different operations route to different datasources.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Create product - routes to MASTER (write).
     */
    @PostMapping
    public Product create(@RequestBody Product product) {
        log.info("POST /api/products -> MASTER");
        return productService.createProduct(product);
    }

    /**
     * Update product - routes to MASTER (write).
     */
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        log.info("PUT /api/products/{} -> MASTER", id);
        return productService.updateProduct(id, product);
    }

    /**
     * Delete product - routes to MASTER (write).
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("DELETE /api/products/{} -> MASTER", id);
        boolean success = productService.deleteProduct(id);
        return Map.of("success", success, "datasource", "master");
    }

    /**
     * Get product by ID - routes to SLAVE (read).
     */
    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        log.info("GET /api/products/{} -> SLAVE", id);
        return productService.getProduct(id);
    }

    /**
     * List all products - routes to SLAVE (read).
     */
    @GetMapping
    public List<Product> list() {
        log.info("GET /api/products -> SLAVE");
        return productService.listAllProducts();
    }

    /**
     * Count products - routes to SLAVE (read).
     */
    @GetMapping("/count")
    public Map<String, Object> count() {
        log.info("GET /api/products/count -> SLAVE");
        return Map.of("count", productService.countAll(), "datasource", "slave");
    }

    /**
     * Dynamic datasource selection using SpEL parameter.
     * GET /api/products/ds/master/1 -> reads from master
     * GET /api/products/ds/slave/1 -> reads from slave
     */
    @GetMapping("/ds/{dsName}/{id}")
    public Product getFromDs(@PathVariable String dsName, @PathVariable Long id) {
        log.info("GET /api/products/ds/{}/{} -> {} (SpEL)", dsName, id, dsName);
        return productService.getProductFromDs(dsName, id);
    }

    /**
     * Manual datasource switching demo.
     * Shows push/pop pattern with DynamicDataSourceContextHolder.
     */
    @GetMapping("/ds/manual/{id}")
    public Product manualSwitch(@PathVariable Long id) {
        log.info("GET /api/products/ds/manual/{} -> MANUAL PUSH/POP", id);
        return productService.getProductManualSwitch(id);
    }

    /**
     * Write-Read split demo: write to master then read from slave.
     */
    @PostMapping("/demo/write-read")
    public Map<String, Object> writeMasterReadSlave(@RequestBody Product product) {
        log.info("POST /api/products/demo/write-read -> MASTER write + SLAVE read");
        Product result = productService.writeMasterReadSlave(product);
        return Map.of(
            "created_product", result,
            "note", "Written to MASTER, read back from SLAVE"
        );
    }
}
