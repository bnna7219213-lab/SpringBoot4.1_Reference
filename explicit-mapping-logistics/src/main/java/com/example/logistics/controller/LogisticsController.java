package com.example.logistics.controller;

import com.example.logistics.dto.ShipmentDTO;
import com.example.logistics.service.LogisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogisticsController {

    private final LogisticsService service;

    public LogisticsController(LogisticsService service) {
        this.service = service;
    }

    @GetMapping("/shipments")
    public ResponseEntity<List<ShipmentDTO>> getAllShipments() {
        return ResponseEntity.ok(service.getAllShipments());
    }
}
