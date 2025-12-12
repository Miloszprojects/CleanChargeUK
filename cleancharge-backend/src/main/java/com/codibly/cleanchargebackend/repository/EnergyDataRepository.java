package com.codibly.cleanchargebackend.repository;

import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;

import java.time.Instant;
import java.util.List;

public interface EnergyDataRepository {

    List<GenerationIntervalDto> getNationalGeneration(Instant from, Instant to);
    List<GenerationIntervalDto> getRegionalGeneration(RegionScope region, Instant from, Instant to);
    List<GenerationIntervalDto> getCiRegionGeneration(int ciRegionId, Instant from, Instant to);
}
