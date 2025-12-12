package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.api.dto.OptimalChargingWindowResponseDto;
import com.codibly.cleanchargebackend.api.mapper.ChargingWindowApiMapper;
import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.service.OptimalChargingWindowService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/optimal-charging-window")
@RequiredArgsConstructor
public class ChargingWindowController {

    private final OptimalChargingWindowService service;

    @GetMapping
    public OptimalChargingWindowResponseDto getOptimalWindow(
            @RequestParam int hours,
            @RequestParam(defaultValue = "NATIONAL") RegionScope region
    ) {
        var window = service.findOptimalWindow(hours, region);
        return ChargingWindowApiMapper.toResponse(window);
    }
}
