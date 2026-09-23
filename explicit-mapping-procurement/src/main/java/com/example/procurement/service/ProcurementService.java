package com.example.procurement.service;

import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.dto.SupplierDTO;
import com.example.procurement.entity.PurchaseRequestEntity;
import com.example.procurement.entity.SupplierEntity;
import com.example.procurement.mapper.PurchaseRequestMapper;
import com.example.procurement.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcurementService {

    private final SupplierMapper supplierMapper;
    private final PurchaseRequestMapper purchaseRequestMapper;
    private final List<SupplierEntity> supplierStore = new ArrayList<>();
    private final List<PurchaseRequestEntity> prStore = new ArrayList<>();

    public ProcurementService(SupplierMapper supplierMapper, PurchaseRequestMapper purchaseRequestMapper) {
        this.supplierMapper = supplierMapper;
        this.purchaseRequestMapper = purchaseRequestMapper;
        seed();
    }

    private void seed() {
        supplierStore.add(SupplierEntity.builder()
                .id(3001L).supplierName("Shenzhen Electronics Co.")
                .supplierCode("SUP-SZ-ELC-001")
                .unifiedSocialCode("91440300MA5G8XXXXX")
                .contactPerson("David Chen").contactPhone("1*********8")
                .bankName("China Merchants Bank").bankAccount("7559****1234")
                .qualificationLevel("A").cooperationStatus(1)
                .registeredCapital(new BigDecimal("5000000"))
                .establishedDate(LocalDate.of(2015, 3, 10))
                .lastEvaluationScore(new BigDecimal("92.5"))
                .build());

        prStore.add(PurchaseRequestEntity.builder()
                .id(4001L).prNo("PR-20240115-0042")
                .applicantName("Alice Zhang").department("Engineering")
                .itemsJson("[{\"name\":\"Laptop\",\"qty\":5,\"unitPrice\":8999.99}]")
                .totalBudget(new BigDecimal("72498.90")).urgencyLevel(2)
                .approvalChainJson("{\"stages\":[\"Manager\",\"Director\",\"VP\"],\"approved\":[\"Manager\",\"Director\"]}")
                .status(1).approvedBy("Bob Director")
                .approvedAt(LocalDateTime.of(2024, 1, 14, 16, 30))
                .expectedDeliveryDate(LocalDate.of(2024, 2, 1))
                .build());
    }

    public List<SupplierDTO> getAllSuppliers() {
        return supplierStore.stream().map(supplierMapper::toSupplierDTO).toList();
    }

    public List<PurchaseRequestDTO> getAllPurchaseRequests() {
        return prStore.stream().map(purchaseRequestMapper::toPurchaseRequestDTO).toList();
    }
}
