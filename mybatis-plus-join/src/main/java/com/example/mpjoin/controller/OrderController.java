package com.example.mpjoin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mpjoin.dto.OrderDetailDTO;
import com.example.mpjoin.entity.Order;
import com.example.mpjoin.service.OrderService;
import com.example.mpjoin.service.ProductSalesVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order Controller - Multi-table JOIN queries via REST API.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>3-table JOIN (Order + OrderItem + Product)</li>
 *   <li>LEFT JOIN with aggregation</li>
 *   <li>Pagination with joins</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Get order detail with all items and product info (3-table INNER JOIN).
     * GET /api/orders/{id}/detail
     */
    @GetMapping("/{id}/detail")
    public OrderDetailDTO getOrderDetail(@PathVariable Long id) {
        log.info("GET /api/orders/{}/detail", id);
        return orderService.getOrderDetail(id);
    }

    /**
     * Get all orders with full details.
     * GET /api/orders/details
     */
    @GetMapping("/details")
    public List<OrderDetailDTO> listAllOrderDetails() {
        log.info("GET /api/orders/details");
        return orderService.listAllOrderDetails();
    }

    /**
     * Paginated orders with detail (JOIN + pagination).
     * GET /api/orders?pageNum=1&pageSize=10&customer=zhang
     */
    @GetMapping
    public IPage<Order> pageOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String customer) {
        log.info("GET /api/orders page={} size={} customer={}", pageNum, pageSize, customer);
        return orderService.pageOrdersWithDetails(pageNum, pageSize, customer);
    }

    /**
     * Product sales report (LEFT JOIN + aggregation).
     * GET /api/orders/products/sales
     */
    @GetMapping("/products/sales")
    public List<ProductSalesVO> getProductSales() {
        log.info("GET /api/orders/products/sales");
        return orderService.getProductSales();
    }

    /**
     * Get order by order number (INNER JOIN).
     * GET /api/orders/by-no/ORD-2024-001
     */
    @GetMapping("/by-no/{orderNo}")
    public OrderDetailDTO getOrderByNo(@PathVariable String orderNo) {
        log.info("GET /api/orders/by-no/{}", orderNo);
        return orderService.getOrderByNo(orderNo);
    }
}
