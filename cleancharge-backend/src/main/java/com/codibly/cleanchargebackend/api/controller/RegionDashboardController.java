package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.api.dto.RegionDashboardSummaryDto;
import com.codibly.cleanchargebackend.service.RegionDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionDashboardController {

    private final RegionDashboardService regionDashboardService;

    @GetMapping("/{regionId}/summary")
    public RegionDashboardSummaryDto getRegionSummary(@PathVariable int regionId) {
        return regionDashboardService.getRegionSummary(regionId);
    }
}
