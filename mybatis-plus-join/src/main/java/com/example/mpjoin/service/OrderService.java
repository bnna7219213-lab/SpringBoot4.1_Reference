package com.example.mpjoin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mpjoin.dto.OrderDetailDTO;
import com.example.mpjoin.entity.Order;

import java.util.List;

/**
 * Order Service interface demonstrating JOIN operations.
 *
 * <p>Uses MPJLambdaJoinWrapper for type-safe multi-table queries.
 */
public interface OrderService {

    /**
     * Get order with all items and product details (INNER JOIN).
     *
     * @param orderId the order ID
     * @return order detail DTO
     */
    OrderDetailDTO getOrderDetail(Long orderId);

    /**
     * Get all orders with full details (3-table JOIN).
     *
     * @return list of order details
     */
    List<OrderDetailDTO> listAllOrderDetails();

    /**
     * Paginated query with joined tables.
     *
     * @param pageNum  page number
     * @param pageSize page size
     * @param customer optional customer name filter
     * @return paginated orders
     */
    IPage<Order> pageOrdersWithDetails(int pageNum, int pageSize, String customer);

    /**
     * LEFT JOIN: get all products with their total sales quantity.
     *
     * @return list of products with sales info
     */
    List<ProductSalesVO> getProductSales();

    /**
     * INNER JOIN: get order with items and products.
     *
     * @param orderNo order number
     * @return order detail
     */
    OrderDetailDTO getOrderByNo(String orderNo);
}
