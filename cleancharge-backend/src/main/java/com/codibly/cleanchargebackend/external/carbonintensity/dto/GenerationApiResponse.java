package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record GenerationApiResponse(
        List<GenerationIntervalDto> data
) {}
