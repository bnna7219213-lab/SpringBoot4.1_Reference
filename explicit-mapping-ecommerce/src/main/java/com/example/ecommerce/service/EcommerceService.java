package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.entity.OrderEntity;
import com.example.ecommerce.entity.OrderItemEntity;
import com.example.ecommerce.entity.ProductEntity;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EcommerceService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final List<ProductEntity> productStore = new ArrayList<>();
    private final List<OrderEntity> orderStore = new ArrayList<>();

    public EcommerceService(ProductMapper productMapper, OrderMapper orderMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        seed();
    }

    private void seed() {
        productStore.add(ProductEntity.builder()
                .id(1L).name("Wireless Mouse").sku("WM-2024-BLK")
                .unitPrice(new BigDecimal("12.99")).stock(50)
                .weight(new BigDecimal("0.085")).categoryCode("ELEC-ACC")
                .status(1).createTime(LocalDateTime.of(2024, 1, 10, 9, 0))
                .build());
        productStore.add(ProductEntity.builder()
                .id(2L).name("USB-C Hub").sku("HUB-7IN1")
                .unitPrice(new BigDecimal("63.99")).stock(0)
                .weight(new BigDecimal("0.12")).categoryCode("ELEC-ACC")
                .status(2).createTime(LocalDateTime.of(2024, 1, 11, 10, 0))
                .build());

        List<OrderItemEntity> items = new ArrayList<>();
        items.add(OrderItemEntity.builder().id(1L).productName("Wireless Mouse")
                .quantity(2).unitPrice(new BigDecimal("12.99")).build());
        items.add(OrderItemEntity.builder().id(2L).productName("USB-C Hub")
                .quantity(1).unitPrice(new BigDecimal("63.99")).build());

        orderStore.add(OrderEntity.builder()
                .id(1L).orderNo("ORD-20240115-A001").userId(100L)
                .totalAmount(new BigDecimal("89.97")).discountAmount(new BigDecimal("10.00"))
                .status(1).items(items)
                .shippingProvince("Guangdong").shippingCity("Shenzhen")
                .shippingDetail("Nanshan Tech Park Tower B")
                .createTime(LocalDateTime.of(2024, 1, 15, 14, 30))
                .build());
    }

    public List<ProductDTO> getAllProducts() {
        return productStore.stream().map(productMapper::toProductDTO).toList();
    }

    public Optional<ProductDTO> getProductById(Long id) {
        return productStore.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(productMapper::toProductDTO);
    }

    public List<OrderDTO> getAllOrders() {
        return orderStore.stream().map(orderMapper::toOrderDTO).toList();
    }
}
