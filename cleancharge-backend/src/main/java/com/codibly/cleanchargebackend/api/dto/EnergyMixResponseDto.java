package com.codibly.cleanchargebackend.api.dto;

import java.util.List;

public record EnergyMixResponseDto(
        List<EnergyMixDayDto> days
) {}
