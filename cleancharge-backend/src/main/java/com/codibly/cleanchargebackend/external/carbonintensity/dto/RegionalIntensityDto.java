package com.codibly.cleanchargebackend.external.carbonintensity.dto;

public record RegionalIntensityDto(
        Integer forecast,
        Integer actual,
        String index
) {}
