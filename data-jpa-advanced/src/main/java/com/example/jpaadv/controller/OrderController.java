package com.example.jpaadv.controller;

import com.example.jpaadv.entity.Order;
import com.example.jpaadv.projection.OrderSummary;
import com.example.jpaadv.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for Order operations.
 *
 * Endpoints mirror the service layer capabilities:
 * - POST /api/orders           — create order with pessimistic lock
 * - POST /api/orders/optimistic — create order with optimistic lock retry
 * - GET  /api/orders/summaries  — DTO projection (readOnly)
 * - GET  /api/orders/{customerName} /paged — pagination
 * - GET  /api/orders/scroll     — Slice-based scroll pagination
 * - GET  /api/orders/statistics — Native SQL aggregation
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam String orderNo,
                                             @RequestParam String customerName,
                                             @RequestParam String shippingAddress,
                                             @RequestParam Long productId,
                                             @RequestParam int quantity) {
        Order order = orderService.createOrder(orderNo, customerName, shippingAddress, productId, quantity);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/optimistic")
    public ResponseEntity<Order> createOrderOptimistic(@RequestParam String orderNo,
                                                       @RequestParam String customerName,
                                                       @RequestParam String shippingAddress,
                                                       @RequestParam Long productId,
                                                       @RequestParam int quantity) {
        Order order = orderService.createOrderWithRetry(orderNo, customerName, shippingAddress, productId, quantity);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/summaries")
    public List<OrderSummary> getSummaries(@RequestParam(defaultValue = "PAID") String status) {
        return orderService.findOrderSummaries(status);
    }

    @GetMapping("/summariesPaged")
    public Page<OrderSummary> getSummariesPaged(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return orderService.findOrderSummariesPaged(org.springframework.data.domain.PageRequest.of(page, size));
    }

    @GetMapping("/scroll")
    public Slice<Order> scrollOrders(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return orderService.scrollOrders(page, size);
    }

    @GetMapping("/statistics")
    public List<Object[]> getOrderStatistics() {
        return orderService.getOrderStatistics();
    }

    @GetMapping("/byCustomerPaged")
    public Page<Order> getByCustomerPaged(@RequestParam String customerName,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return orderService.findOrdersByCustomerPaged(customerName, page, size);
    }
}
