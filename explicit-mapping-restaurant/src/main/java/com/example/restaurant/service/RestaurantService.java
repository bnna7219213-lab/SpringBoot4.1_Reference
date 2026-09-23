package com.example.restaurant.service;

import com.example.restaurant.dto.DishDTO;
import com.example.restaurant.dto.FoodOrderDTO;
import com.example.restaurant.entity.DishEntity;
import com.example.restaurant.entity.FoodOrderEntity;
import com.example.restaurant.mapper.DishMapper;
import com.example.restaurant.mapper.FoodOrderMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RestaurantService {

    private final DishMapper dishMapper;
    private final FoodOrderMapper foodOrderMapper;
    private final List<DishEntity> dishStore = new ArrayList<>();
    private final List<FoodOrderEntity> orderStore = new ArrayList<>();
    private final AtomicLong dishIdSeq = new AtomicLong(2001);
    private final AtomicLong orderIdSeq = new AtomicLong(8001);

    public RestaurantService(DishMapper dishMapper, FoodOrderMapper foodOrderMapper) {
        this.dishMapper = dishMapper;
        this.foodOrderMapper = foodOrderMapper;
        seedDishes();
        seedOrders();
    }

    private void seedDishes() {
        dishStore.add(DishEntity.builder()
                .id(dishIdSeq.getAndIncrement()).dishName("Mapo Tofu").dishCode("D-MAPO-001")
                .categoryId(3L).price(new BigDecimal("1999")).cost(new BigDecimal("850"))
                .stock(12).isSpicy(2).allergens("peanut,soy")
                .description("Classic Sichuan tofu")
                .imageUrl("https://cdn.example.com/mapo-tofu.jpg")
                .createdAt(LocalDateTime.of(2024, 1, 14, 10, 0))
                .specs("{\"size\":[\"small\",\"large\"]}")
                .build());
        dishStore.add(DishEntity.builder()
                .id(dishIdSeq.getAndIncrement()).dishName("Kung Pao Chicken").dishCode("D-KUNG-002")
                .categoryId(3L).price(new BigDecimal("2499")).cost(new BigDecimal("1200"))
                .stock(8).isSpicy(1).allergens("peanut")
                .description("Spicy stir-fried chicken with peanuts")
                .imageUrl("https://cdn.example.com/kungpao.jpg")
                .createdAt(LocalDateTime.of(2024, 1, 14, 11, 0))
                .specs("{\"size\":[\"small\"]}")
                .build());
    }

    private void seedOrders() {
        orderStore.add(FoodOrderEntity.builder()
                .id(orderIdSeq.getAndIncrement()).orderNo("ORD-RES-20240115-001")
                .customerId(5001L).customerName("Charlie Li")
                .itemsJson("[{\"name\":\"Mapo Tofu\",\"qty\":2,\"price\":1999},{\"name\":\"Kung Pao Chicken\",\"qty\":1,\"price\":2499}]")
                .totalAmount(new BigDecimal("6497")).tableNumber(5)
                .bookingTime(LocalDateTime.of(2024, 1, 15, 18, 30))
                .remark("No cilantro").status("PREPARING")
                .createdAt(LocalDateTime.of(2024, 1, 15, 18, 25))
                .build());
    }

    public List<DishDTO> getAllDishes() {
        return dishStore.stream().map(dishMapper::toDishDTO).toList();
    }

    public Optional<DishDTO> getDishById(Long id) {
        return dishStore.stream().filter(d -> d.getId().equals(id)).findFirst().map(dishMapper::toDishDTO);
    }

    public List<FoodOrderDTO> getAllOrders() {
        return orderStore.stream().map(foodOrderMapper::toFoodOrderDTO).toList();
    }
}
