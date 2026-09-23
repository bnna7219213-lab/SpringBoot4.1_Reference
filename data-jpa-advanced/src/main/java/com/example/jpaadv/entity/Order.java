package com.example.jpaadv.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order entity demonstrating:
 * - OneToMany with OrderItem (cascade + orphan removal)
 * - Composite-valued @OneToMany mapping via MapsId
 * - @Version optimistic locking
 * - @Lock(PESSIMISTIC_WRITE) for inventory deduction
 * - System.out.println("OrderRepository.findById(id, LockModeType.PESSIMISTIC_WRITE)")
 * - JPA lifecycle callbacks: @PostLoad, @PrePersist, @PostPersist, @PreUpdate
 * - JPA auditing
 *
 * The @Transient totalAmount is computed from line items in @PostLoad.
 */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Column(name = "customer_name", nullable = false, length = 128)
    private String customerName;

    /**
     * Order status codes:
     *  PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "total_amount", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    /**
     * OneToMany with cascade-all and orphanRemoval.
     * Changes to items are cascaded; removing from list deletes the row.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id.productId ASC")
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Optimistic locking — JPA increments this on every UPDATE.
     * Concurrent modification throws ObjectOptimisticLockingFailureException.
     */
    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Transient flag set by @PostLoad to indicate the order was fully loaded.
     */
    @Transient
    private boolean loadedFromDb;

    protected Order() {}

    public Order(String orderNo, String customerName, String shippingAddress) {
        this.orderNo = orderNo;
        this.customerName = customerName;
        this.shippingAddress = shippingAddress;
    }

    // --- Relationship helpers ---

    public void addItem(Product product, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem(this, product, quantity, unitPrice);
        items.add(item);
        recalculateTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- JPA Lifecycle Callbacks ---

    /**
     * @PrePersist runs before the INSERT statement.
     * Use case: validate business state, set defaults that depend on other fields.
     */
    @PrePersist
    void prePersist() {
        recalculateTotal();
        if (status == null) {
            status = "PENDING";
        }
    }

    /**
     * @PostPersist runs after the INSERT succeeds.
     * Use case: logging, post-creation side effects (e.g., publish domain event).
     */
    @PostPersist
    void postPersist() {
        // Demo: in production, publish OrderCreatedEvent
        System.out.println("[POST-PERSIST] New order persisted: id=" + id + ", orderNo=" + orderNo);
    }

    /**
     * @PreUpdate runs before the UPDATE statement.
     * Use case: recompute derived fields before flush.
     */
    @PreUpdate
    void preUpdate() {
        recalculateTotal();
    }

    /**
     * @PostLoad runs after entity state is loaded from the database.
     * Use case: initialize transient fields, cache derived state.
     */
    @PostLoad
    void postLoad() {
        this.loadedFromDb = true;
        // Force-load lazy items list for totalAmount calculation
        recalculateTotal();
    }

    // --- Getters / Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public Long getVersion() { return version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public boolean isLoadedFromDb() { return loadedFromDb; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Order{id=%d, orderNo='%s', customer='%s', status='%s', items=%d, total=%s, version=%d}"
                .formatted(id, orderNo, customerName, status, items.size(), totalAmount, version);
    }
}
