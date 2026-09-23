package com.example.procurement.mapper;

import com.example.procurement.converter.ProcurementConverters;
import com.example.procurement.dto.SupplierDTO;
import com.example.procurement.entity.SupplierEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = ProcurementConverters.class)
public interface SupplierMapper {

    @Mapping(source = "unifiedSocialCode", target = "maskedTaxId", qualifiedByName = "maskTaxId")
    @Mapping(source = "contactPerson", target = "contact")
    @Mapping(source = "contactPerson", target = "contact")
    @Mapping(source = "cooperationStatus", target = "statusText", qualifiedByName = "statusToText")
    @Mapping(source = "qualificationLevel", target = "levelScore")
    @Mapping(source = "lastEvaluationScore", target = "lastScore", qualifiedByName = "scoreToDisplay")
    @Mapping(target = "bankInfo", ignore = true)
    @Mapping(target = "levelScore", ignore = true)
    @Mapping(target = "isQualified", ignore = true)
    @Mapping(target = "establishedYears", ignore = true)
    @Mapping(target = "contact", ignore = true)
    SupplierDTO toSupplierDTO(SupplierEntity entity);

    @AfterMapping
    default void fillComputedFields(SupplierEntity entity, @MappingTarget SupplierDTO dto) {
        // Multi-field merge: bankName + masked bankAccount
        dto.setBankInfo(ProcurementConverters.mergeBankInfo(entity.getBankName(), entity.getBankAccount()));
        // Computed: isQualified = !D && status==1
        dto.setQualified(ProcurementConverters.computeIsQualified(entity.getQualificationLevel(), entity.getCooperationStatus()));
        // Computed: establishedYears = now - establishedDate.year
        dto.setEstablishedYears(ProcurementConverters.dateToYearsSince(entity.getEstablishedDate()));
        // Multi-field merge: contactPerson + contactPhone
        dto.setContact(entity.getContactPerson() + " (" + entity.getContactPhone() + ")");
        // Level score: qualification level + description
        dto.setLevelScore(getLevelDescription(entity.getQualificationLevel()));
    }

    private static String getLevelDescription(String level) {
        if (level == null) return "Unknown";
        return switch (level) {
            case "A" -> "A (Excellent)";
            case "B" -> "B (Good)";
            case "C" -> "C (Average)";
            case "D" -> "D (Poor - Not Preferred)";
            default -> level;
        };
    }
}
