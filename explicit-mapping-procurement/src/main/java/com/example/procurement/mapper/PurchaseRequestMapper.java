package com.example.procurement.mapper;

import com.example.procurement.converter.ProcurementConverters;
import com.example.procurement.dto.PurchaseRequestDTO;
import com.example.procurement.entity.PurchaseRequestEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = "spring", uses = ProcurementConverters.class)
public interface PurchaseRequestMapper {

    @Mapping(source = "prNo", target = "prNumber")
    @Mapping(source = "applicantName", target = "applicant")
    @Mapping(source = "department", target = "departmentDisplay")
    @Mapping(source = "totalBudget", target = "totalBudgetFormatted", qualifiedByName = "budgetToFormatted")
    @Mapping(source = "urgencyLevel", target = "urgencyText", qualifiedByName = "urgencyToText")
    @Mapping(source = "status", target = "statusText", qualifiedByName = "statusToText")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "approvalProgress", ignore = true)
    @Mapping(target = "approvedByAt", ignore = true)
    @Mapping(target = "deliveryDaysRemaining", ignore = true)
    @Mapping(target = "overdueFlag", ignore = true)
    @Mapping(target = "complianceIndicator", ignore = true)
    PurchaseRequestDTO toPurchaseRequestDTO(PurchaseRequestEntity entity);

    @AfterMapping
    default void fillComputedFields(PurchaseRequestEntity entity, @MappingTarget PurchaseRequestDTO dto) {
        // Parse approval chain JSON -> progress "2/5"
        if (entity.getApprovalChainJson() != null) {
            dto.setApprovalProgress(parseApprovalProgress(entity.getApprovalChainJson()));
        }
        // Merge: approvedBy + approvedAt
        dto.setApprovedByAt(ProcurementConverters.mergeApprovedByAt(entity.getApprovedBy(), entity.getApprovedAt()));
        // Computed: deliveryDaysRemaining
        long days = ProcurementConverters.dateToDaysRemaining(entity.getExpectedDeliveryDate());
        dto.setDeliveryDaysRemaining(days);
        // Computed: overdueFlag = days < 0
        dto.setOverdueFlag(days < 0);
        // Computed: complianceIndicator
        dto.setComplianceIndicator(ProcurementConverters.computeCompliance(entity.getUrgencyLevel(), days));
        // Parse items JSON -> List<PurchaseItemDTO>
        if (entity.getItemsJson() != null) {
            dto.setItems(parseItems(entity.getItemsJson()));
        }
    }

    private static String statusToText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "已批准";
            case 3 -> "已拒绝";
            default -> "未知";
        };
    }

    private static String parseApprovalProgress(String chainJson) {
        try {
            // Simple pattern-based counting (production: use objectMapper)
            long stages = Pattern.compile("\"([^\"]+)\"")
                    .matcher(chainJson).results().count();
            // Approved count — find "approved":[...]
            Matcher approvedMatcher = Pattern.compile("\"approved\":\\s*\\[([^\\]]*)\\]").matcher(chainJson);
            long approved = 0;
            if (approvedMatcher.find()) {
                String list = approvedMatcher.group(1);
                approved = list.isEmpty() ? 0 :
                        Pattern.compile("\"([^\"]+)\"").matcher(list).results().count();
            }
            return approved + "/" + stages;
        } catch (Exception e) {
            return "0/0";
        }
    }

    private static List<PurchaseRequestDTO.PurchaseItemDTO> parseItems(String itemsJson) {
        // Simplified parsing — production: use objectMapper
        try {
            Pattern p = Pattern.compile(
                    "\"name\":\\s*\"([^\"]+)\"[^}]*\"qty\":\\s*(\\d+)[^}]*\"unitPrice\":\\s*([\\d.]+)");
            Matcher m = p.matcher(itemsJson);
            java.util.ArrayList<PurchaseRequestDTO.PurchaseItemDTO> items = new java.util.ArrayList<>();
            while (m.find()) {
                var unitPrice = new java.math.BigDecimal(m.group(3));
                int qty = Integer.parseInt(m.group(2));
                items.add(PurchaseRequestDTO.PurchaseItemDTO.builder()
                        .name(m.group(1)).quantity(qty).unitPrice(unitPrice)
                        .subtotal(unitPrice.multiply(java.math.BigDecimal.valueOf(qty)))
                        .build());
            }
            return items;
        } catch (Exception e) {
            return java.util.List.of();
        }
    }
}
