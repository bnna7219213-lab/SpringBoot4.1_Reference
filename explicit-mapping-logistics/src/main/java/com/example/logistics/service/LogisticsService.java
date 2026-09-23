package com.example.logistics.service;

import com.example.logistics.dto.ShipmentDTO;
import com.example.logistics.entity.ShipmentEntity;
import com.example.logistics.entity.TrackerEventEntity;
import com.example.logistics.mapper.ShipmentMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LogisticsService {

    private final ShipmentMapper shipmentMapper;
    private final List<ShipmentEntity> shipmentStore = new ArrayList<>();

    public LogisticsService(ShipmentMapper shipmentMapper) {
        this.shipmentMapper = shipmentMapper;
        seed();
    }

    private void seed() {
        List<TrackerEventEntity> events = new ArrayList<>();
        events.add(TrackerEventEntity.builder()
                .id(1L).shipmentCode("SF-20240115-78234").eventLevel(0)
                .eventTime(LocalDateTime.of(2024, 1, 15, 9, 0))
                .locationDesc("Beijing Distribution Center")
                .lat(new BigDecimal("39.904200")).lng(new BigDecimal("116.407400"))
                .operator("SYSTEM").build());

        shipmentStore.add(ShipmentEntity.builder()
                .id(7001L).shipmentCode("SF-20240115-78234")
                .senderName("Alice Zhang").senderPhone("1*********4")
                .senderLat(new BigDecimal("39.904200")).senderLng(new BigDecimal("116.407400"))
                .receiverName("Bob Wang").receiverPhone("1*********1")
                .receiverLat(new BigDecimal("22.543100")).receiverLng(new BigDecimal("114.057900"))
                .currentState(1).weight(new BigDecimal("2.5")).volume(new BigDecimal("0.015"))
                .price(new BigDecimal("25.50")).timeline(events)
                .createTime(LocalDateTime.of(2024, 1, 15, 8, 30))
                .build());
    }

    public List<ShipmentDTO> getAllShipments() {
        return shipmentStore.stream().map(shipmentMapper::toShipmentDTO).toList();
    }
}
