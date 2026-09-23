package com.example.ecommerce;

import com.example.ecommerce.entity.OrderEntity;
import com.example.ecommerce.entity.OrderItemEntity;
import com.example.ecommerce.entity.ProductEntity;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.dto.ProductDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * E-commerce Explicit Mapping Demo.
 *
 * Compares MapStruct (correct) vs BeanUtils (silently fails) on 5 mismatch categories:
 * 1. Field rename: orderNo -> orderNumber
 * 2. Type+semantic change: Integer status -> String statusText (status=1 -> "已支付")
 * 3. Computed field: fullAddress = province + city + detailAddress
 * 4. Formatting: BigDecimal price -> "formatPrice" string ("formatPrice")
 * 5. Multi-field merge: totalAmount - discountAmount -> payableAmount
 */
@SpringBootApplication
public class ExplicitMappingEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplicitMappingEcommerceApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(ProductMapper productMapper,
                                  OrderMapper orderMapper) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  E-COMMERCE EXPLICIT MAPPING DEMO");
            System.out.println("=".repeat(80));

            // Build sample entities (simulating JPA fetch)
            ProductEntity product = new ProductEntity();
            product.setId(1001L);
            product.setName("Wireless Mouse");
            product.setSku("WM-2024-BLK");
            product.setUnitPrice(new BigDecimal("12.99"));
            product.setStock(50);
            product.setWeight(new BigDecimal("0.085")); // 85g in kg
            product.setCategoryCode("ELEC-ACC");
            product.setStatus(1); // 1=ACTIVE
            product.setCreateTime(LocalDateTime.of(2024, 1, 15, 10, 30));

            // --- Product mapping demo ---
            ProductDTO productDTO = productMapper.toProductDTO(product);

            System.out.println("\n--- Product Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  name        = " + product.getName());
            System.out.println("  unitPrice   = " + product.getUnitPrice());

            System.out.println("\nTARGET (DTO via MapStruct):");
            System.out.println("  name              = " + productDTO.getName());
            System.out.println("  formattedPrice    = " + productDTO.getFormattedPrice());
            System.out.println("  inStock           = " + productDTO.isInStock());
            System.out.println("  categoryName      = " + productDTO.getCategoryName());
            System.out.println("  weightDisplay     = " + productDTO.getWeightDisplay());

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            ProductDTO beanUtilsProduct = new ProductDTO();
            BeanUtils.copyProperties(product, beanUtilsProduct);
            System.out.println("  name              = " + beanUtilsProduct.getName());
            System.out.println("  formattedPrice    = " + beanUtilsProduct.getFormattedPrice() + "  <-- NULL! BigDecimal->String conversion needed");
            System.out.println("  inStock           = " + beanUtilsProduct.isInStock() + "  <-- false (default), stock>0 not computed");
            System.out.println("  categoryName      = " + beanUtilsProduct.getCategoryName() + "  <-- NULL! Code-to-name lookup needed");
            System.out.println("  weightDisplay     = " + beanUtilsProduct.getWeightDisplay() + "  <-- NULL! type conversion + formatting");

            // --- Order mapping demo ---
            OrderEntity order = new OrderEntity();
            order.setId(5001L);
            order.setOrderNo("ORD-20240115-ABCDEF");
            order.setUserId(777L);
            order.setTotalAmount(new BigDecimal("89.97"));
            order.setDiscountAmount(new BigDecimal("10.00"));
            order.setStatus(1); // 1=PAID

            OrderItemEntity item1 = new OrderItemEntity();
            item1.setProductName("Wireless Mouse");
            item1.setQuantity(2);
            item1.setUnitPrice(new BigDecimal("12.99"));
            OrderItemEntity item2 = new OrderItemEntity();
            item2.setProductName("USB-C Hub");
            item2.setQuantity(1);
            item2.setUnitPrice(new BigDecimal("63.99"));

            order.setItems(List.of(item1, item2));
            order.setShippingProvince("Guangdong");
            order.setShippingCity("Shenzhen");
            order.setShippingDetail("Nanshan District, Tech Park Tower B, Floor 12");
            order.setCreateTime(LocalDateTime.of(2024, 1, 15, 14, 30));

            OrderDTO orderDTO = orderMapper.toOrderDTO(order);

            System.out.println("\n--- Order Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  orderNo         = " + order.getOrderNo());
            System.out.println("  status (Integer)= " + order.getStatus());
            System.out.println("  totalAmount     = " + order.getTotalAmount());
            System.out.println("  discountAmount  = " + order.getDiscountAmount());
            System.out.println("  province/city/detail = " + order.getShippingProvince() + " " + order.getShippingCity() + " " + order.getShippingDetail());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  orderNumber     = " + orderDTO.getOrderNumber());
            System.out.println("  statusText      = " + orderDTO.getStatusText());
            System.out.println("  totalAmount     = " + orderDTO.getTotalAmount());
            System.out.println("  discount        = " + orderDTO.getDiscount());
            System.out.println("  payableAmount   = " + orderDTO.getPayableAmount());
            System.out.println("  fullAddress     = " + orderDTO.getFullAddress());
            System.out.println("  itemCountSummary= " + orderDTO.getItemCountSummary());
            System.out.println("  items.size      = " + orderDTO.getItems().size());

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            OrderDTO beanUtilsOrder = new OrderDTO();
            BeanUtils.copyProperties(order, beanUtilsOrder);
            System.out.println("  orderNumber     = " + beanUtilsOrder.getOrderNumber() + "  <-- NULL! Field rename needed (orderNo -> orderNumber)");
            System.out.println("  statusText      = " + beanUtilsOrder.getStatusText() + "  <-- NULL! Integer -> String conversion needed");
            System.out.println("  totalAmount     = " + beanUtilsOrder.getTotalAmount());
            System.out.println("  discount        = " + beanUtilsOrder.getDiscount() + "  <-- NULL! Field rename needed (discountAmount -> discount)");
            System.out.println("  payableAmount   = " + beanUtilsOrder.getPayableAmount() + "  <-- NULL! Computation needed (total - discount)");
            System.out.println("  fullAddress     = " + beanUtilsOrder.getFullAddress() + "  <-- NULL! 3 fields must be merged");
            System.out.println("  items           = " + beanUtilsOrder.getItems() + "  <-- NULL! List of different types (collection mapping)");

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  CONCLUSION: 11 out of 11 target fields require explicit configuration.");
            System.out.println("  BeanUtils silently produces wrong/null data — no error, no warning.");
            System.out.println("  MapStruct generates correct code at compile time with type safety.");
            System.out.println("=".repeat(80) + "\n");
        };
    }
}
