package com.example.mpjoin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mpjoin.dto.OrderDetailDTO;
import com.example.mpjoin.entity.Order;
import com.example.mpjoin.entity.OrderItem;
import com.example.mpjoin.entity.Product;
import com.example.mpjoin.mapper.OrderMapper;
import com.example.mpjoin.service.OrderService;
import com.example.mpjoin.service.ProductSalesVO;
import com.github.yulichang.query.MPJLambdaJoinWrapper;
import com.github.yulichang.toolkit.MPJWrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Order Service Implementation with JOIN queries.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li>MPJLambdaJoinWrapper for type-safe joins</li>
 *   <li>INNER JOIN with selectAs for DTO projection</li>
 *   <li>LEFT JOIN with aggregate functions</li>
 *   <li>Pagination with joins</li>
 *   <li>3-table joins (Order + OrderItem + Product)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId) {
        log.info("Fetching order detail for id={}", orderId);

        // 3-table INNER JOIN: Order + OrderItem + Product
        return orderMapper.selectJoinOne(OrderDetailDTO.class,
            MPJWrappers.<Order>lambdaJoin()
                // Select order fields with aliases
                .selectAs(Order::getId, OrderDetailDTO::setOrderId)
                .selectAs(Order::getOrderNo, OrderDetailDTO::setOrderNo)
                .selectAs(Order::getCustomerName, OrderDetailDTO::setCustomerName)
                .selectAs(Order::getTotalAmount, OrderDetailDTO::setTotalAmount)
                .selectAs(Order::getStatus, OrderDetailDTO::setStatus)
                .selectAs(Order::getCreateTime, OrderDetailDTO::setCreateTime)
                // INNER JOIN t_order_item ON t_order.id = t_order_item.order_id
                .innerJoin(OrderItem.class, OrderItem::getOrderId, Order::getId)
                .selectAs(OrderItem::getId, OrderDetailDTO.OrderItemDetail::setItemId)
                .selectAs(OrderItem::getQuantity, OrderDetailDTO.OrderItemDetail::setQuantity)
                .selectAs(OrderItem::getUnitPrice, OrderDetailDTO.OrderItemDetail::setUnitPrice)
                .selectAs(OrderItem::getSubtotal, OrderDetailDTO.OrderItemDetail::setSubtotal)
                // INNER JOIN t_product ON t_order_item.product_id = t_product.id
                .innerJoin(Product.class, Product::getId, OrderItem::getProductId)
                .selectAs(Product::getId, OrderDetailDTO.OrderItemDetail::setProductId)
                .selectAs(Product::getProductName, OrderDetailDTO.OrderItemDetail::setProductName)
                .selectAs(Product::getCategory, OrderDetailDTO.OrderItemDetail::setCategory)
                .selectAs(Product::getPrice, OrderDetailDTO.OrderItemDetail::setCurrentPrice)
                // WHERE condition
                .eq(Order::getId, orderId)
        );
    }

    @Override
    public List<OrderDetailDTO> listAllOrderDetails() {
        log.info("Fetching all order details with 3-table JOIN");

        return orderMapper.selectJoinList(OrderDetailDTO.class,
            MPJWrappers.<Order>lambdaJoin()
                .selectAs(Order::getId, OrderDetailDTO::setOrderId)
                .selectAs(Order::getOrderNo, OrderDetailDTO::setOrderNo)
                .selectAs(Order::getCustomerName, OrderDetailDTO::setCustomerName)
                .selectAs(Order::getTotalAmount, OrderDetailDTO::setTotalAmount)
                .selectAs(Order::getStatus, OrderDetailDTO::setStatus)
                .selectAs(Order::getCreateTime, OrderDetailDTO::setCreateTime)
                .innerJoin(OrderItem.class, OrderItem::getOrderId, Order::getId)
                .selectAs(OrderItem::getId, OrderDetailDTO.OrderItemDetail::setItemId)
                .selectAs(OrderItem::getQuantity, OrderDetailDTO.OrderItemDetail::setQuantity)
                .selectAs(OrderItem::getUnitPrice, OrderDetailDTO.OrderItemDetail::setUnitPrice)
                .selectAs(OrderItem::getSubtotal, OrderDetailDTO.OrderItemDetail::setSubtotal)
                .innerJoin(Product.class, Product::getId, OrderItem::getProductId)
                .selectAs(Product::getId, OrderDetailDTO.OrderItemDetail::setProductId)
                .selectAs(Product::getProductName, OrderDetailDTO.OrderItemDetail::setProductName)
                .selectAs(Product::getCategory, OrderDetailDTO.OrderItemDetail::setCategory)
                .selectAs(Product::getPrice, OrderDetailDTO.OrderItemDetail::setCurrentPrice)
                .orderByDesc(Order::getCreateTime)
        );
    }

    @Override
    public IPage<Order> pageOrdersWithDetails(int pageNum, int pageSize, String customer) {
        log.info("Paginating orders: page={}, size={}, customer={}", pageNum, pageSize, customer);

        Page<Order> page = new Page<>(pageNum, pageSize);

        MPJLambdaJoinWrapper<Order> wrapper = MPJWrappers.<Order>lambdaJoin()
            .selectAll(Order.class)
            .leftJoin(OrderItem.class, OrderItem::getOrderId, Order::getId)
            .selectAs(OrderItem::getQuantity, OrderItem::setQuantity)
            .selectAs(OrderItem::getSubtotal, OrderItem::setSubtotal)
            .leftJoin(Product.class, Product::getId, OrderItem::getProductId)
            .selectAs(Product::getProductName, Product::setProductName);

        if (customer != null && !customer.isEmpty()) {
            wrapper.like(Order::getCustomerName, customer);
        }

        return orderMapper.selectJoinList(page, Order.class, wrapper);
    }

    @Override
    public List<ProductSalesVO> getProductSales() {
        log.info("Fetching product sales summary with LEFT JOIN");

        // LEFT JOIN: All products with sales (products with no sales show NULLs)
        return orderMapper.selectJoinList(ProductSalesVO.class,
            MPJWrappers.<Order>lambdaJoin()
                // From Product perspective
                .selectAs(Product::getId, ProductSalesVO::setProductId)
                .selectAs(Product::getProductName, ProductSalesVO::setProductName)
                .selectAs(Product::getCategory, ProductSalesVO::setCategory)
                .selectAs(Product::getPrice, ProductSalesVO::setPrice)
                // LEFT JOIN to get sales data (nullable)
                .leftJoin(OrderItem.class, OrderItem::getProductId, Product::getId)
                // Aggregate: sum of quantities sold
                .selectSum(OrderItem::getQuantity, ProductSalesVO::setTotalSold)
                // Aggregate: sum of subtotals as revenue
                .selectSum(OrderItem::getSubtotal, ProductSalesVO::setTotalRevenue)
                .groupBy(Product::getId)
                .orderByDesc(ProductSalesVO::setTotalSold)
        );
    }

    @Override
    public OrderDetailDTO getOrderByNo(String orderNo) {
        log.info("Fetching order by number: {}", orderNo);

        return orderMapper.selectJoinOne(OrderDetailDTO.class,
            MPJWrappers.<Order>lambdaJoin()
                .selectAs(Order::getId, OrderDetailDTO::setOrderId)
                .selectAs(Order::getOrderNo, OrderDetailDTO::setOrderNo)
                .selectAs(Order::getCustomerName, OrderDetailDTO::setCustomerName)
                .selectAs(Order::getTotalAmount, OrderDetailDTO::setTotalAmount)
                .selectAs(Order::getStatus, OrderDetailDTO::setStatus)
                .selectAs(Order::getCreateTime, OrderDetailDTO::setCreateTime)
                .innerJoin(OrderItem.class, OrderItem::getOrderId, Order::getId)
                .selectAs(OrderItem::getId, OrderDetailDTO.OrderItemDetail::setItemId)
                .selectAs(OrderItem::getQuantity, OrderDetailDTO.OrderItemDetail::setQuantity)
                .selectAs(OrderItem::getUnitPrice, OrderDetailDTO.OrderItemDetail::setUnitPrice)
                .selectAs(OrderItem::getSubtotal, OrderDetailDTO.OrderItemDetail::setSubtotal)
                .innerJoin(Product.class, Product::getId, OrderItem::getProductId)
                .selectAs(Product::getId, OrderDetailDTO.OrderItemDetail::setProductId)
                .selectAs(Product::getProductName, OrderDetailDTO.OrderItemDetail::setProductName)
                .selectAs(Product::getCategory, OrderDetailDTO.OrderItemDetail::setCategory)
                .selectAs(Product::getPrice, OrderDetailDTO.OrderItemDetail::setCurrentPrice)
                .eq(Order::getOrderNo, orderNo)
        );
    }
}
