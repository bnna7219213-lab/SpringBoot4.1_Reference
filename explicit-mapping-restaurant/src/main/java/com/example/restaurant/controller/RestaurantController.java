package com.example.restaurant.controller;

import com.example.restaurant.dto.DishDTO;
import com.example.restaurant.dto.FoodOrderDTO;
import com.example.restaurant.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    @GetMapping("/dishes")
    public ResponseEntity<List<DishDTO>> getAllDishes() {
        return ResponseEntity.ok(service.getAllDishes());
    }

    @GetMapping("/dishes/{id}")
    public ResponseEntity<DishDTO> getDishById(@PathVariable Long id) {
        return service.getDishById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/foodOrders")
    public ResponseEntity<List<FoodOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }
}
