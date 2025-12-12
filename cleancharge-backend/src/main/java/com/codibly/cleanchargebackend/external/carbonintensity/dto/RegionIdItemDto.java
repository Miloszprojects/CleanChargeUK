package com.codibly.cleanchargebackend.external.carbonintensity.dto;

import java.util.List;

public record RegionIdItemDto(
        int regionid,
        String dnoregion,
        String shortname,
        List<RegionIdIntervalDto> data
) {}
