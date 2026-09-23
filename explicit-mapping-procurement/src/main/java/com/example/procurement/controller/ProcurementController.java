package com.example.procurement.controller;

import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.dto.SupplierDTO;
import com.example.procurement.service.ProcurementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProcurementController {

    private final ProcurementService service;

    public ProcurementController(ProcurementService service) {
        this.service = service;
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        return ResponseEntity.ok(service.getAllSuppliers());
    }

    @GetMapping("/purchaseRequests")
    public ResponseEntity<List<PurchaseRequestDTO>> getAllPRs() {
        return ResponseEntity.ok(service.getAllPurchaseRequests());
    }
}
