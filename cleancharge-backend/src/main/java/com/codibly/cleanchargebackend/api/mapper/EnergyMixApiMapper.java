package com.codibly.cleanchargebackend.api.mapper;

import com.codibly.cleanchargebackend.api.dto.*;
import com.codibly.cleanchargebackend.domain.DailyEnergyMixSummary;
import com.codibly.cleanchargebackend.domain.FuelType;

import java.util.List;

public final class EnergyMixApiMapper {

    private EnergyMixApiMapper() {}

    public static EnergyMixResponseDto toResponse(List<DailyEnergyMixSummary> summaries) {
        List<EnergyMixDayDto> days = summaries.stream()
                .map(EnergyMixApiMapper::toDay)
                .toList();
        return new EnergyMixResponseDto(days);
    }

    private static EnergyMixDayDto toDay(DailyEnergyMixSummary summary) {
        List<FuelShareDto> mix = summary.averageFuelShare().entrySet().stream()
                .map(e -> new FuelShareDto(
                        fuelLabel(e.getKey()),
                        e.getValue()
                ))
                .toList();

        return new EnergyMixDayDto(
                summary.date(),
                summary.region().name(),
                mix,
                summary.cleanEnergyPercentage()
        );
    }

    private static String fuelLabel(FuelType type) {
        return type.name().toLowerCase();
    }
}
