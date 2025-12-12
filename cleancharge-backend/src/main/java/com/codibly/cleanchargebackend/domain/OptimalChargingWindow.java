package com.codibly.cleanchargebackend.domain;

import java.time.OffsetDateTime;

public record OptimalChargingWindow(
        RegionScope region,
        OffsetDateTime start,
        OffsetDateTime end,
        double averageCleanEnergyPercentage
) {}
