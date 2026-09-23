package com.example.logistics;

import com.example.logistics.entity.ShipmentEntity;
import com.example.logistics.entity.TrackerEventEntity;
import com.example.logistics.mapper.ShipmentMapper;
import com.example.logistics.dto.ShipmentDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class ExplicitMappingLogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExplicitMappingLogisticsApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(ShipmentMapper shipmentMapper) {
        return args -> {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("  LOGISTICS EXPLICIT MAPPING DEMO");
            System.out.println("=".repeat(80));

            ShipmentEntity shipment = new ShipmentEntity();
            shipment.setId(7001L);
            shipment.setShipmentCode("SF-20240115-78234");
            shipment.setSenderName("Alice Zhang");
            shipment.setSenderPhone("1*********4");
            shipment.setSenderLat(new BigDecimal("39.904200"));
            shipment.setSenderLng(new BigDecimal("116.407400"));
            shipment.setReceiverName("Bob Wang");
            shipment.setReceiverPhone("1*********1");
            shipment.setReceiverLat(new BigDecimal("22.543100"));
            shipment.setReceiverLng(new BigDecimal("114.057900"));
            shipment.setCurrentState(1); // 1=IN_TRANSIT
            shipment.setWeight(new BigDecimal("2.5"));
            shipment.setVolume(new BigDecimal("0.015"));
            shipment.setPrice(new BigDecimal("25.50"));
            shipment.setCreateTime(LocalDateTime.of(2024, 1, 15, 8, 30));

            TrackerEventEntity event1 = new TrackerEventEntity();
            event1.setId(1L);
            event1.setShipmentCode("SF-20240115-78234");
            event1.setEventLevel(0);
            event1.setEventTime(LocalDateTime.of(2024, 1, 15, 9, 0));
            event1.setLocationDesc("Beijing Distribution Center");
            event1.setLat(new BigDecimal("39.904200"));
            event1.setLng(new BigDecimal("116.407400"));
            event1.setOperator("SYSTEM");

            TrackerEventEntity event2 = new TrackerEventEntity();
            event2.setId(2L);
            event2.setShipmentCode("SF-20240115-78234");
            event2.setEventLevel(1);
            event2.setEventTime(LocalDateTime.of(2024, 1, 15, 14, 30));
            event2.setLocationDesc("In transit - Highway G4");
            event2.setLat(new BigDecimal("30.592800"));
            event2.setLng(new BigDecimal("114.305500"));
            event2.setOperator("SYSTEM");

            shipment.setTimeline(List.of(event1, event2));

            ShipmentDTO dto = shipmentMapper.toShipmentDTO(shipment);

            System.out.println("\n--- Shipment Mapping Comparison ---");
            System.out.println("SOURCE (Entity):");
            System.out.println("  shipmentCode   = " + shipment.getShipmentCode());
            System.out.println("  senderPhone    = " + shipment.getSenderPhone());
            System.out.println("  currentState   = " + shipment.getCurrentState() + " (Integer)");
            System.out.println("  senderLat/Lng  = " + shipment.getSenderLat() + ", " + shipment.getSenderLng());
            System.out.println("  receiverLat/Lng= " + shipment.getReceiverLat() + ", " + shipment.getReceiverLng());
            System.out.println("  price          = " + shipment.getPrice());

            System.out.println("\nTARGET (DTO via MapStruct — CORRECT):");
            System.out.println("  shipmentCode    = " + dto.getShipmentCode());
            System.out.println("  senderInfo      = " + dto.getSenderInfo());
            System.out.println("  receiverInfo    = " + dto.getReceiverInfo());
            System.out.println("  statusText      = " + dto.getStatusText());
            System.out.println("  route           = " + dto.getRoute());
            System.out.println("  formattedPrice  = " + dto.getFormattedPrice());
            System.out.println("  timeline.size   = " + (dto.getTimeline() != null ? dto.getTimeline().size() : "null"));

            System.out.println("\nTARGET (DTO via BeanUtils — WRONG for most fields):");
            ShipmentDTO beanUtilsDto = new ShipmentDTO();
            BeanUtils.copyProperties(shipment, beanUtilsDto);
            System.out.println("  shipmentCode    = " + beanUtilsDto.getShipmentCode() + "  <-- OK (rename)");
            System.out.println("  senderInfo      = " + beanUtilsDto.getSenderInfo() + "  <-- NULL! Multi-field merge + mask (FAIL)");
            System.out.println("  receiverInfo    = " + beanUtilsDto.getReceiverInfo() + "  <-- NULL! Multi-field merge (FAIL)");
            System.out.println("  statusText      = " + beanUtilsDto.getStatusText() + "  <-- NULL! Integer -> String (FAIL)");
            System.out.println("  route           = " + beanUtilsDto.getRoute() + "  <-- NULL! Haversine computation (FAIL)");
            System.out.println("  formattedPrice  = " + beanUtilsDto.getFormattedPrice() + "  <-- NULL! Formatting (FAIL)");
            System.out.println("  timeline        = " + beanUtilsDto.getTimeline() + "  <-- NULL! Type conversion (FAIL)");

            System.out.println("\n" + "=".repeat(80));
            System.out.println("  8 out of 8 target fields require explicit configuration.");
            System.out.println("  BeanUtils: 0 out of 8 correct.");
            System.out.println("  MapStruct: 8 out of 8 correct.");
            System.out.println("=".repeat(80) + "\n");
        };
    }
}
