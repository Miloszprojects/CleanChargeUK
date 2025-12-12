package com.codibly.cleanchargebackend.api.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyEnergyMixDto(
        LocalDate date,
        double cleanPercentage,
        List<FuelShareDto> mix
) {}
