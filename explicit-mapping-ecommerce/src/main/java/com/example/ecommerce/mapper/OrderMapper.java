package com.example.ecommerce.mapper;

import com.example.ecommerce.converter.EcommerceConverters;
import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.dto.OrderItemDTO;
import com.example.ecommerce.entity.OrderEntity;
import com.example.ecommerce.entity.OrderItemEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * MapStruct mapper for OrderEntity -> Order.
 *
 * Demonstrates ALL 5 categories of mismatch:
 * 1. @Mapping(source="orderNo", target="orderNumber")
 * 2. @Mapping(target="statusText", source="status", qualifiedByName="statusToText")
 * 3. @AfterMapping: payableAmount = totalAmount - discountAmount
 * 4. @Mapping(target="createTimeStr", source="createTime", qualifiedByName="dateTimeToFormatted")
 * 5. @AfterMapping: fullAddress = province + city + detail
 * 6. List mapping: List<OrderItemEntity> -> List<OrderItemDTO> (auto via MapStruct)
 */
@Mapper(componentModel = "spring",
        uses = EcommerceConverters.class,
        imports = BigDecimal.class)
public interface OrderMapper {

    @Mapping(source = "orderNo", target = "orderNumber")
    @Mapping(source = "discountAmount", target = "discount")
    @Mapping(source = "status", target = "statusText", qualifiedByName = "statusToText")
    @Mapping(source = "createTime", target = "createTimeStr", qualifiedByName = "dateTimeToFormatted")
    @Mapping(target = "payableAmount", ignore = true)
    @Mapping(target = "fullAddress", ignore = true)
    @Mapping(target = "itemCountSummary", ignore = true)
    @Mapping(target = "items", ignore = true)
    OrderDTO toOrderDTO(OrderEntity entity);

    @Mapping(target = "subtotal", source = "unitPrice")
    @Mapping(target = "subtotal", expression = "java(entity.getUnitPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())))")
    OrderItemDTO toOrderItemDTO(OrderItemEntity entity);

    /**
     * For single item subtotal computation (helper for list mapping).
     */
    @Named("computeSubtotal")
    default BigDecimal computeSubtotal(OrderItemEntity entity) {
        if (entity.getUnitPrice() == null || entity.getQuantity() == null) return BigDecimal.ZERO;
        return entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
    }

    /**
     * After-source-mapping: fill computed fields that require multiple source fields.
     */
    @AfterMapping
    default void fillComputedFields(OrderEntity entity, @MappingTarget OrderDTO dto) {
        // Computed: payableAmount = totalAmount - discountAmount
        if (entity.getTotalAmount() != null) {
            BigDecimal discount = entity.getDiscountAmount() != null ? entity.getDiscountAmount() : BigDecimal.ZERO;
            dto.setPayableAmount(entity.getTotalAmount().subtract(discount));
        }

        // Merge 3 fields: fullAddress = province + city + detail
        StringBuilder address = new StringBuilder();
        if (entity.getShippingProvince() != null) address.append(entity.getShippingProvince());
        if (entity.getShippingCity() != null) address.append(entity.getShippingCity());
        if (entity.getShippingDetail() != null) address.append(entity.getShippingDetail());
        dto.setFullAddress(address.length() > 0 ? address.toString() : "无地址信息");

        // Aggregate from list: itemCountSummary
        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            long totalUnits = entity.getItems().stream()
                    .filter(i -> i.getQuantity() != null)
                    .mapToLong(OrderItemEntity::getQuantity)
                    .sum();
            int itemCount = entity.getItems().size();
            dto.setItemCountSummary("%d 件商品, 共 %d 件".formatted(itemCount, totalUnits));
        } else {
            dto.setItemCountSummary("0 件商品");
        }

        // Map items list (auto-map via MapStruct since field name is same: items -> items)
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(this::toOrderItemDTOExplicit)
                    .toList());
        }
    }

    /**
     * Explicit list-item mapping: unitPrice * quantity -> subtotal.
     * Note: This version computes subtotal automatically.
     */
    private OrderItemDTO toOrderItemDTOExplicit(OrderItemEntity entity) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(entity.getId());
        dto.setProductName(entity.getProductName());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        // REQUIRES: unitPrice * quantity -> subtotal (computed field)
        if (entity.getUnitPrice() != null && entity.getQuantity() != null) {
            dto.setSubtotal(entity.getUnitPrice().multiply(BigDecimal.valueOf(entity.getQuantity())));
        }
        return dto;
    }
}
