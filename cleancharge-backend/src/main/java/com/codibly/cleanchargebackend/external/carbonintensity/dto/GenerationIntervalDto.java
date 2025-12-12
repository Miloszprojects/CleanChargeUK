package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record GenerationIntervalDto(
        String from,
        String to,
        List<GenerationMixDto> generationmix
) {}
