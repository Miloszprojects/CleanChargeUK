package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record RegionalIntervalDto(
        String from,
        String to,
        List<RegionDto> regions
) {}
