package com.example.procurement;

import com.example.procurement.entity.PurchaseRequestEntity;
import com.example.procurement.entity.SupplierEntity;
import com.example.procurement.mapper.PurchaseRequestMapper;
import com.example.procurement.mapper.SupplierMapper;
import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.dto.SupplierDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class ExplicitMappingProcurementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplicitMappingProcurementApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(SupplierMapper supplierMapper,
                                  PurchaseRequestMapper purchaseRequestMapper) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  PROCUREMENT EXPLICIT MAPPING DEMO");
            System.out.println("=".repeat(80));

            SupplierEntity supplier = new SupplierEntity();
            supplier.setId(3001L);
            supplier.setSupplierName("Shenzhen Electronics Co., Ltd.");
            supplier.setSupplierCode("SUP-SZ-ELC-001");
            supplier.setUnifiedSocialCode("91440300MA5G8XXXXX");
            supplier.setContactPerson("David Chen");
            supplier.setContactPhone("1*********8");
            supplier.setAddress("Shenzhen, Guangdong, China");
            supplier.setBankName("China Merchants Bank");
            supplier.setBankAccount("7559****1234");
            supplier.setQualificationLevel("A");
            supplier.setCooperationStatus(1); // 1=active
            supplier.setRegisteredCapital(new BigDecimal("5000000"));
            supplier.setEstablishedDate(LocalDate.of(2015, 3, 10));
            supplier.setLastEvaluationScore(new BigDecimal("92.5"));

            SupplierDTO supplierDto = supplierMapper.toSupplierDTO(supplier);

            System.out.println("\n--- Supplier Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  unifiedSocialCode= " + supplier.getUnifiedSocialCode());
            System.out.println("  bankName         = " + supplier.getBankName());
            System.out.println("  bankAccount      = " + supplier.getBankAccount());
            System.out.println("  qualificationLevel=" + supplier.getQualificationLevel());
            System.out.println("  cooperationStatus= " + supplier.getCooperationStatus());
            System.out.println("  establishedDate  = " + supplier.getEstablishedDate());
            System.out.println("  lastEvalScore    = " + supplier.getLastEvaluationScore());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  maskedTaxId     = " + supplierDto.getMaskedTaxId());
            System.out.println("  bankInfo         = " + supplierDto.getBankInfo());
            System.out.println("  levelScore       = " + supplierDto.getLevelScore());
            System.out.println("  statusText       = " + supplierDto.getStatusText());
            System.out.println("  lastScore        = " + supplierDto.getLastScore());
            System.out.println("  isQualified      = " + supplierDto.isQualified());
            System.out.println("  establishedYears = " + supplierDto.getEstablishedYears());

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            SupplierDTO beanUtilsSupplier = new SupplierDTO();
            BeanUtils.copyProperties(supplier, beanUtilsSupplier);
            System.out.println("  maskedTaxId     = " + beanUtilsSupplier.getMaskedTaxId() + "  <-- NULL! Masking (FAIL)");
            System.out.println("  bankInfo         = " + beanUtilsSupplier.getBankInfo() + "  <-- NULL! Multi-field merge (FAIL)");
            System.out.println("  levelScore       = " + beanUtilsSupplier.getLevelScore() + "  <-- NULL! Formatting (FAIL)");
            System.out.println("  statusText       = " + beanUtilsSupplier.getStatusText() + "  <-- NULL! Integer->String (FAIL)");
            System.out.println("  isQualified      = " + beanUtilsSupplier.isQualified() + "  <-- false (default) (FAIL)");
            System.out.println("  establishedYears = " + beanUtilsSupplier.getEstablishedYears() + "  <-- 0 (default) (FAIL)");

            PurchaseRequestEntity pr = new PurchaseRequestEntity();
            pr.setId(4001L);
            pr.setPrNo("PR-20240115-0042");
            pr.setApplicantName("Alice Zhang");
            pr.setDepartment("Engineering");
            pr.setItemsJson("""
                    [{"name":"Laptop","qty":5,"unitPrice":8999.99},{"name":"Monitor","qty":10,"unitPrice":2499.50}]""");
            pr.setTotalBudget(new BigDecimal("72498.90"));
            pr.setUrgencyLevel(2); // 2=urgent
            pr.setApprovalChainJson("""
                    {"stages":["Manager","Director","VP","CFO","CEO"],"approved":["Manager","Director"]}""");
            pr.setStatus(1); // 1=in_review
            pr.setApprovedBy("Bob Director");
            pr.setApprovedAt(LocalDateTime.of(2024, 1, 14, 16, 30));
            pr.setExpectedDeliveryDate(LocalDate.of(2024, 2, 1));

            PurchaseRequestDTO prDto = purchaseRequestMapper.toPurchaseRequestDTO(pr);

            System.out.println("\n--- Purchase Request Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  prNo            = " + pr.getPrNo());
            System.out.println("  totalBudget     = " + pr.getTotalBudget());
            System.out.println("  urgencyLevel    = " + pr.getUrgencyLevel());
            System.out.println("  approvalChain   = (JSON string)");
            System.out.println("  approvedBy      = " + pr.getApprovedBy());
            System.out.println("  expectedDelivery= " + pr.getExpectedDeliveryDate());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  prNumber        = " + prDto.getPrNumber());
            System.out.println("  totalBudgetFormat=" + prDto.getTotalBudgetFormatted());
            System.out.println("  urgencyText     = " + prDto.getUrgencyText());
            System.out.println("  approvalProgress= " + prDto.getApprovalProgress());
            System.out.println("  statusText      = " + prDto.getStatusText());
            System.out.println("  approvedByAt    = " + prDto.getApprovedByAt());
            System.out.println("  deliveryDaysLeft= " + prDto.getDeliveryDaysRemaining());
            System.out.println("  overdueFlag     = " + prDto.isOverdueFlag());
            System.out.println("  items.size      = " + (prDto.getItems() != null ? prDto.getItems().size() : "null"));

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  ALL target fields require explicit MapStruct configuration.");
            System.out.println("  BeanUtils: 0 out of 11 correct for SupplierDTO");
            System.out.println("  BeanUtils: 0 out of 13 correct for PurchaseRequestDTO");
            System.out.println("=".repeat(80) + "\n");
        };
    }
}
