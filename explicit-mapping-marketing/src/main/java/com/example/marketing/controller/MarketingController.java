package com.example.marketing.controller;

import com.example.marketing.dto.ActivityDTO;
import com.example.marketing.dto.CampaignDTO;
import com.example.marketing.service.MarketingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketingController {

    private final MarketingService service;

    public MarketingController(MarketingService service) {
        this.service = service;
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignDTO>> getAllCampaigns() {
        return ResponseEntity.ok(service.getAllCampaigns());
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        return ResponseEntity.ok(service.getAllActivities());
    }
}
