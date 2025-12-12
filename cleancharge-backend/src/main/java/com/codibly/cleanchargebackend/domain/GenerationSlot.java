package com.codibly.cleanchargebackend.domain;

import java.time.OffsetDateTime;
import java.util.Map;

public record GenerationSlot(
        OffsetDateTime from,
        OffsetDateTime to,
        Map<FuelType, Double> fuelShare
) {}
