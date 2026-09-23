package com.example.logistics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerEventEntity {

    private Long id;
    private String shipmentCode;
    private Integer eventLevel; // 0=NORMAL, 1=WARNING, 2=ERROR
    private LocalDateTime eventTime;
    private String locationDesc;
    private BigDecimal lat;
    private BigDecimal lng;
    private String operator;
}
