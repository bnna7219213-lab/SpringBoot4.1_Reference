package com.example.jpaadv.spec;

import com.example.jpaadv.entity.Order;
import com.example.jpaadv.entity.OrderItem;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query builder using JPA Specifications.
 *
 * Specifications allow composing WHERE conditions at runtime — each method
 * returns a SpecificationPredicate. Callers chain them with .and() / .or().
 *
 * Usage in repo: orderRepo.findAll(
 *     OrderSpecification.hasStatus("PAID")
 *         .and(OrderSpecification.createdBetween(start, end))
 *         .and(OrderSpecification.totalGreaterThan(new BigDecimal("100")))
 * );
 */
public final class OrderSpecification {

    private OrderSpecification() {}

    /**
     * Filter by exact status match.
     */
    public static Specification<Order> hasStatus(String status) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Filter by customer name (case-insensitive, partial match).
     */
    public static Specification<Order> customerNameLike(String nameFragment) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (nameFragment == null || nameFragment.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("customerName")),
                    "%" + nameFragment.toLowerCase() + "%");
        };
    }

    /**
     * Filter orders created between two timestamps (inclusive).
     */
    public static Specification<Order> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter orders where totalAmount exceeds a minimum.
     * Demonstrates aggregate comparison — uses SUM of item subtotals.
     */
    public static Specification<Order> totalGreaterThan(BigDecimal minTotal) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (minTotal == null) return cb.conjunction();
            // SUM(subtotal) > minTotal via GROUP BY with HAVING
            var itemJoin = root.join("items", JoinType.LEFT);
            var subTotalPath = itemJoin.get("subtotal");

            query.groupBy(root.get("id"));
            query.having(cb.greaterThan(cb.sum(subTotalPath), minTotal));

            // Ensure distinct results (since we joined)
            query.distinct(true);
            return cb.conjunction();
        };
    }

    /**
     * Filter orders that contain a specific product.
     * Demonstrates JOIN + IN usage on a collection relationship.
     */
    public static Specification<Order> containsProduct(Long productId) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (productId == null) return cb.conjunction();
            Join<Order, OrderItem> itemJoin = root.join("items", JoinType.INNER);
            return cb.equal(itemJoin.get("product").get("id"), productId);
        };
    }

    /**
     * Filter orders containing any of the given statuses.
     * Demonstrates IN clause.
     */
    public static Specification<Order> statusIn(List<String> statuses) {
        return (Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("status").in(statuses);
        };
    }

    /**
     * Composite: filter by status range AND minimum total — for reporting.
     */
    public static Specification<Order> completedOrders(LocalDateTime since) {
        return hasStatus("DELIVERED").and(createdBetween(since, null));
    }
}
