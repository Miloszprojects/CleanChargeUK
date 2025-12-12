package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record RegionIdIntervalDto(
        String from,
        String to,
        RegionalIntensityDto intensity,
        List<GenerationMixDto> generationmix
) {}
