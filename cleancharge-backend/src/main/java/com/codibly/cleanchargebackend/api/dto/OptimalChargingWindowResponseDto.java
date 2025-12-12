package com.codibly.cleanchargebackend.api.dto;

import java.time.OffsetDateTime;

public record OptimalChargingWindowResponseDto(
        String region,
        OffsetDateTime start,
        OffsetDateTime end,
        double averageCleanEnergyPercentage
) {}
