package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record RegionalGenerationApiResponse(
        List<RegionalIntervalDto> data
) {}