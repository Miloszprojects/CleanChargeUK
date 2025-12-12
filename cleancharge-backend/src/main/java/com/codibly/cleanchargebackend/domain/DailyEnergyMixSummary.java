package com.codibly.cleanchargebackend.domain;

import java.time.LocalDate;
import java.util.Map;

public record DailyEnergyMixSummary(
        LocalDate date,
        RegionScope region,
        Map<FuelType, Double> averageFuelShare,
        double cleanEnergyPercentage
) {}
