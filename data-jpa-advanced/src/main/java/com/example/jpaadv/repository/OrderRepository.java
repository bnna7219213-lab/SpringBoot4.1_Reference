package com.example.jpaadv.repository;

import com.example.jpaadv.entity.Order;
import com.example.jpaadv.projection.OrderSummary;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderRepository demonstrates:
 * - JpaSpecificationExecutor<Order> for dynamic query composition
 * - @Lock(PESSIMISTIC_WRITE) for pessimistic locking
 * - @Query with JPQL (custom queries)
 * - Native SQL query with @Query(nativeQuery = true)
 * - Slice<Order> for efficient scroll-based pagination
 * - DTO projection (OrderSummary) via constructor expression
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Pessimistic write lock — blocks concurrent transactions from modifying
     * the same row until this transaction commits.
     *
     * Usage: service layer calls this within @Transactional to safely deduct inventory.
     * SQL generated: SELECT ... FROM orders WHERE id = ? FOR UPDATE
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    /**
     * Derived query with pagination + sorting.
     * Page<T> includes total element count (extra COUNT query).
     */
    Page<Order> findByCustomerName(String customerName, Pageable pageable);

    /**
     * Find by status with sorting.
     */
    List<Order> findByStatus(String status, Sort sort);

    /**
     * Custom JPQL: find orders created between two timestamps.
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Order> findByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Custom JPQL with DTO projection.
     * The NEW constructor expression creates OrderSummary instances directly.
     */
    @Query("SELECT new com.example.jpaadv.projection.OrderSummary(" +
           "o.id, o.orderNo, o.status, SIZE(o.items), o.totalAmount) " +
           "FROM Order o WHERE o.status = :status")
    List<OrderSummary> findSummariesByStatus(@Param("status") String status);

    /**
     * Custom JPQL with DTO projection and pagination.
     */
    @Query("SELECT new com.example.jpaadv.projection.OrderSummary(" +
           "o.id, o.orderNo, o.status, SIZE(o.items), o.totalAmount) FROM Order o")
    Page<OrderSummary> findOrderSummaries(Pageable pageable);

    /**
     * Slice-based pagination — more efficient than Page because it does NOT
     * execute a COUNT(*) query. Useful for "load more" / infinite scroll UIs.
     * hasNext() can be checked without counting total rows.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC")
    Slice<Order> findOrdersScroll(Pageable pageable);

    /**
     * Native SQL query for reports where JPQL is insufficient.
     * Maps result columns to a scalar Object[].
     */
    @Query(value = "SELECT status, COUNT(*) as order_count, COALESCE(SUM(total_amount), 0) as total " +
                   "FROM orders GROUP BY status ORDER BY status",
            nativeQuery = true)
    List<Object[]> findOrderStatisticsNative();

    /**
     * Native SQL UPDATE with positional parameters.
     * Requires @Modifying annotation at the call site or in a separate
     * repository method. Shown here as query definition only.
     */
    @Query(value = "UPDATE orders SET status = ?1 WHERE id = ?2", nativeQuery = true)
    int updateOrderStatusNative(String status, Long orderId);
}
