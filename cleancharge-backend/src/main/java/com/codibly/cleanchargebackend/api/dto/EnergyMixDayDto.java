package com.codibly.cleanchargebackend.api.dto;

import java.time.LocalDate;
import java.util.List;

public record EnergyMixDayDto(
        LocalDate date,
        String region,
        List<FuelShareDto> mix,
        double cleanEnergyPercentage
) {}