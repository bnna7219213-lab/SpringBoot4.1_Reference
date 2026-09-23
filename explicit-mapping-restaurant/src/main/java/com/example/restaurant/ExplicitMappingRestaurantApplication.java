package com.example.restaurant;

import com.example.restaurant.entity.DishEntity;
import com.example.restaurant.entity.FoodOrderEntity;
import com.example.restaurant.mapper.DishMapper;
import com.example.restaurant.mapper.FoodOrderMapper;
import com.example.restaurant.dto.DishOrderDTO;
import com.example.restaurant.dto.DishDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class ExplicitMappingRestaurantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplicitMappingRestaurantApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(DishMapper dishMapper,
                                  FoodOrderMapper foodOrderMapper) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  RESTAURANT EXPLICIT MAPPING DEMO");
            System.out.println("=".repeat(80));

            DishEntity dish = new DishEntity();
            dish.setId(2001L);
            dish.setDishName("Mapo Tofu");
            dish.setDishCode("DISH-MAPO-001");
            dish.setCategoryId(3L);
            dish.setPrice(new BigDecimal("1999")); // 1999 cents = ¥19.99
            dish.setCost(new BigDecimal("850"));   // 850 cents cost
            dish.setStock(12);
            dish.setIsSpicy(2); // 2=medium spicy
            dish.setAllergens("peanut,soy");
            dish.setDescription("Classic Sichuan tofu dish with bold spicy flavor");
            dish.setImageUrl("https://cdn.example.com/dishes/mapo-tofu.jpg");
            dish.setCreatedAt(LocalDateTime.of(2024, 1, 14, 10, 0));
            dish.setSpecs("""
                    {"size": ["small", "large"], "extra_cheese": true, "spicy_level": [1,2,3]}""");

            DishDTO dishDto = dishMapper.toDishDTO(dish);

            System.out.println("\n--- Dish Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  price (cents)   = " + dish.getPrice() + " (Integer cents)");
            System.out.println("  isSpicy         = " + dish.getIsSpicy());
            System.out.println("  allergens       = " + dish.getAllergens());
            System.out.println("  cost (cents)    = " + dish.getCost());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  displayName     = " + dishDto.getDisplayName());
            System.out.println("  displayPrice    = " + dishDto.getDisplayPrice());
            System.out.println("  priceLevel      = " + dishDto.getPriceLevel());
            System.out.println("  spiceLevel      = " + dishDto.getSpiceLevel());
            System.out.println("  allergenTags    = " + dishDto.getAllergenTags());
            System.out.println("  profitMargin    = " + dishDto.getProfitMargin() + "%");

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            DishDTO beanUtilsDish = new DishDTO();
            BeanUtils.copyProperties(dish, beanUtilsDish);
            System.out.println("  displayName     = " + beanUtilsDish.getDisplayName() + "  <-- NULL! Category emoji prefix (FAIL)");
            System.out.println("  displayPrice    = " + beanUtilsDish.getDisplayPrice() + "  <-- NULL! Cents->yuan conversion (FAIL)");
            System.out.println("  spiceLevel      = " + beanUtilsDish.getSpiceLevel() + "  <-- NULL! Integer->Chinese (FAIL)");
            System.out.println("  allergenTags    = " + beanUtilsDish.getAllergenTags() + "  <-- NULL! CSV->List conversion (FAIL)");

            FoodOrderEntity order = new FoodOrderEntity();
            order.setId(8001L);
            order.setOrderNo("ORD-RES-20240115-001");
            order.setCustomerId(5001L);
            order.setCustomerName("Charlie Li");
            order.setItemsJson("""
                    [{"name":"Mapo Tofu","qty":2,"price":1999},{"name":"Kung Pao Chicken","qty":1,"price":2499}]""");
            order.setTotalAmount(new BigDecimal("6497")); // ¥64.97
            order.setTableNumber(5);
            order.setBookingTime(LocalDateTime.of(2024, 1, 15, 18, 30));
            order.setRemark("No extra cilantro");
            order.setStatus("PREPARING");
            order.setCreatedAt(LocalDateTime.of(2024, 1, 15, 18, 25));

            DishOrderDTO orderDto = foodOrderMapper.toFoodOrderDTO(order);

            System.out.println("\n--- Food Order Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  orderNo         = " + order.getOrderNo());
            System.out.println("  totalAmount (cents)=" + order.getTotalAmount());
            System.out.println("  tableNumber     = " + order.getTableNumber());
            System.out.println("  bookingTime     = " + order.getBookingTime());
            System.out.println("  status          = " + order.getStatus());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  orderNumber     = " + orderDto.getOrderNumber());
            System.out.println("  totalPrice      = " + orderDto.getTotalPrice());
            System.out.println("  tableDisplay    = " + orderDto.getTableDisplay());
            System.out.println("  waitMinutes     = " + orderDto.getWaitMinutes());
            System.out.println("  statusBadge     = " + orderDto.getStatusBadge());
            System.out.println("  items.size      = " + (orderDto.getItems() != null ? orderDto.getItems().size() : "null"));
            System.out.println("  itemSummary     = " + orderDto.getItemSummary());

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            DishOrderDTO beanUtilsOrder = new DishOrderDTO();
            BeanUtils.copyProperties(order, beanUtilsOrder);
            System.out.println("  orderNumber     = " + beanUtilsOrder.getOrderNumber() + "  <-- NULL! Rename (FAIL)");
            System.out.println("  totalPrice      = " + beanUtilsOrder.getTotalPrice() + "  <-- NULL! Cents->yuan (FAIL)");
            System.out.println("  tableDisplay    = " + beanUtilsOrder.getTableDisplay() + "  <-- NULL! Format (FAIL)");
            System.out.println("  waitMinutes     = " + beanUtilsOrder.getWaitMinutes() + "  <-- NULL! Computation (FAIL)");
            System.out.println("  isUrgent        = " + beanUtilsOrder.isUrgent() + "  <-- default false (FAIL — should be true)");

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  ALL target fields require explicit MapStruct configuration.");
            System.out.println("".repeat(80) + "\n");
        };
    }
}
