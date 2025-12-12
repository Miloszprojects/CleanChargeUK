package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.api.dto.EnergyMixResponseDto;
import com.codibly.cleanchargebackend.api.mapper.EnergyMixApiMapper;
import com.codibly.cleanchargebackend.service.EnergyMixService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/energy-mix")
@RequiredArgsConstructor
public class EnergyMixController {

    private final EnergyMixService energyMixService;

    @GetMapping
    public EnergyMixResponseDto getEnergyMix() {
        var summaries = energyMixService.getThreeDayMix();
        return EnergyMixApiMapper.toResponse(summaries);
    }
}
