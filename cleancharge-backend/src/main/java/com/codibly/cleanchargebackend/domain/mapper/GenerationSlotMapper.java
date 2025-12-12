package com.codibly.cleanchargebackend.domain.mapper;

import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.GenerationSlot;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationMixDto;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GenerationSlotMapper {

    private GenerationSlotMapper() {}

    public static GenerationSlot toDomain(GenerationIntervalDto dto) {
        OffsetDateTime from = OffsetDateTime.parse(dto.from());
        OffsetDateTime to   = OffsetDateTime.parse(dto.to());

        Map<FuelType, Double> share = new EnumMap<>(FuelType.class);
        for (GenerationMixDto mix : dto.generationmix()) {
            FuelType type = FuelType.fromApi(mix.fuel());
            share.put(type, mix.perc());
        }

        return new GenerationSlot(from, to, share);
    }

    public static List<GenerationSlot> toDomain(List<GenerationIntervalDto> dtos) {
        return dtos.stream()
                .map(GenerationSlotMapper::toDomain)
                .toList();
    }
}
