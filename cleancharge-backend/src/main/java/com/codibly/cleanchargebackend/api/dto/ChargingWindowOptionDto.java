package com.codibly.cleanchargebackend.api.dto;

import java.time.OffsetDateTime;

public record ChargingWindowOptionDto(
        int hours,
        OffsetDateTime start,
        OffsetDateTime end,
        double averageCleanEnergyPercentage
) {}
