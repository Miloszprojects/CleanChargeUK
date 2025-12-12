package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record RegionDto(
        int regionid,
        String dnoregion,
        String shortname,
        RegionalIntensityDto intensity,
        List<GenerationMixDto> generationmix
) {}
