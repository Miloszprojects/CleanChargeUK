package com.codibly.cleanchargebackend.api.dto;

import java.util.List;

public record RegionDashboardSummaryDto(
        int regionId,
        String name,
        List<FuelShareDto> currentMix,
        List<ChargingWindowOptionDto> chargingWindows,
        List<DailyEnergyMixDto> dailyMixes
) {}