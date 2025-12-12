package com.codibly.cleanchargebackend.api.mapper;

import com.codibly.cleanchargebackend.api.dto.OptimalChargingWindowResponseDto;
import com.codibly.cleanchargebackend.domain.OptimalChargingWindow;

public final class ChargingWindowApiMapper {

    private ChargingWindowApiMapper() {}

    public static OptimalChargingWindowResponseDto toResponse(OptimalChargingWindow window) {
        return new OptimalChargingWindowResponseDto(
                window.region().name(),
                window.start(),
                window.end(),
                window.averageCleanEnergyPercentage()
        );
    }
}
