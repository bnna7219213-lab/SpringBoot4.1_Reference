package com.example.jpaadv.service;

import com.example.jpaadv.entity.Order;
import com.example.jpaadv.entity.Product;
import com.example.jpaadv.projection.OrderSummary;
import com.example.jpaadv.repository.OrderRepository;
import com.example.jpaadv.repository.ProductRepository;
import com.example.jpaadv.spec.OrderSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer showcasing:
 *
 * 1. Pessimistic locking (PESSIMISTIC_WRITE) for safe inventory deduction
 * 2. Optimistic locking retry on ObjectOptimisticLockingFailureException
 * 3. Batch processing with EntityManager
 * 4. ReadOnly transactions for report queries
 * 5. Specification-based dynamic query composition
 * 6. Slice-based pagination for infinite scroll
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EntityManager entityManager;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.entityManager = entityManager;
    }

    // --- PESSIMISTIC LOCKING DEMO ---

    /**
     * Create order with pessimistic lock on product rows.
     *
     * Flow:
     * 1. Begin transaction
     * 2. SELECT ... FOR UPDATE locks the product row
     * 3. Check stock → reduce → save → commit → release lock
     *
     * Concurrent callers block until this transaction completes.
     */
    @Transactional
    public Order createOrder(String orderNo, String customerName, String shippingAddress,
                             Long productId, int quantity) {
        // Pessimistic write lock prevents concurrent stock modifications
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        // Verify stock under lock
        product.reduceStock(quantity);
        productRepository.save(product);

        // Create order
        Order order = new Order(orderNo, customerName, shippingAddress);
        order.addItem(product, quantity, product.getPrice());

        return orderRepository.save(order);
    }

    /**
     * Create order with optimistic locking retry.
     * Uses @Version on Product — no explicit SELECT FOR UPDATE.
     */
    @Transactional
    public Order createOrderWithRetry(String orderNo, String customerName, String shippingAddress,
                                     Long productId, int quantity) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return doCreateOrder(orderNo, customerName, shippingAddress, productId, quantity);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt == maxRetries) {
                    throw new IllegalStateException(
                            "Failed to create order after %d retries due to concurrent modification"
                                    .formatted(maxRetries), e);
                }
                // Brief backoff before retry
                try { Thread.sleep(50L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private Order doCreateOrder(String orderNo, String customerName, String shippingAddress,
                                Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        product.reduceStock(quantity);
        productRepository.save(product);

        Order order = new Order(orderNo, customerName, shippingAddress);
        order.addItem(product, quantity, product.getPrice());
        return orderRepository.save(order);
    }

    // --- BATCH PROCESSING DEMO ---

    /**
     * Batch update status for orders matching criteria.
     *
     * Uses EntityManager directly for bulk operations — avoids loading
     * full entities into memory. Flushes and clears session periodically
     * to prevent memory issues with large batches.
     */
    @Transactional
    public int batchUpdateStatus(List<Long> orderIds, String newStatus) {
        int batchSize = 50;
        int totalUpdated = 0;

        for (Long orderId : orderIds) {
            Order order = entityManager.find(Order.class, orderId);
            if (order != null) {
                order.setStatus(newStatus);
                entityManager.persist(order);
                totalUpdated++;
            }

            // Flush + clear to manage memory for large datasets
            if (totalUpdated % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        return totalUpdated;
    }

    // --- READ-ONLY REPORT QUERIES ---

    /**
     * Read-only transaction for reporting.
     * readOnly=true: no dirty checking, potential performance optimization.
     */
    @Transactional(readOnly = true)
    public List<OrderSummary> findOrderSummaries(String status) {
        return orderRepository.findSummariesByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> findOrderSummariesPaged(Pageable pageable) {
        return orderRepository.findOrderSummaries(pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> findOrderSummariesByStatus(String status) {
        return orderRepository.findSummariesByStatus(status);
    }

    // --- PAGINATION & SORTING ---

    @Transactional(readOnly = true)
    public Page<Order> findOrdersByCustomerPaged(String customerName, int page, int size) {
        return orderRepository.findByCustomerName(customerName,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * Slice-based pagination — efficient "load more" without COUNT query.
     */
    @Transactional(readOnly = true)
    public Slice<Order> scrollOrders(int page, int size) {
        return orderRepository.findOrdersScroll(PageRequest.of(page, size));
    }

    // --- SPECIFICATION QUERIES ---

    @Transactional(readOnly = true)
    public List<Order> findBySpecification(String statusFragment, LocalDateTime start, LocalDateTime end) {
        var spec = OrderSpecification.hasStatus(statusFragment)
                .and(OrderSpecification.createdBetween(start, end));
        return orderRepository.findAll(spec);
    }

    // --- NATIVE QUERY DEMO ---

    @Transactional(readOnly = true)
    public List<Object[]> getOrderStatistics() {
        return orderRepository.findOrderStatisticsNative();
    }
}
