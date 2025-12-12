package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.domain.*;
import com.codibly.cleanchargebackend.domain.mapper.GenerationSlotMapper;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptimalChargingWindowService {

    private final EnergyDataRepository repository;

    public OptimalChargingWindow findOptimalWindow(int hours, RegionScope region) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("hours must be between 1 and 6");
        }

        Instant from = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant to = from.plus(48, ChronoUnit.HOURS);

        List<GenerationIntervalDto> intervals =
                (region == RegionScope.NATIONAL)
                        ? repository.getNationalGeneration(from, to)
                        : repository.getRegionalGeneration(region, from, to);

        var slots = GenerationSlotMapper.toDomain(intervals);

        int slotCount = slots.size();
        int windowSize = hours * 2;

        if (slotCount < windowSize) {
            throw new IllegalStateException("Not enough data for requested window");
        }

        double[] cleanPerc = new double[slotCount];

        for (int i = 0; i < slotCount; i++) {
            GenerationSlot slot = slots.get(i);
            double sum = slot.fuelShare().entrySet().stream()
                    .filter(e -> e.getKey().isClean())
                    .mapToDouble(e -> e.getValue())
                    .sum();
            cleanPerc[i] = sum;
        }

        double currentSum = 0.0;
        for (int i = 0; i < windowSize; i++) {
            currentSum += cleanPerc[i];
        }

        double bestSum = currentSum;
        int bestStart = 0;

        for (int start = 1; start <= slotCount - windowSize; start++) {
            currentSum = currentSum - cleanPerc[start - 1] + cleanPerc[start + windowSize - 1];
            if (currentSum > bestSum) {
                bestSum = currentSum;
                bestStart = start;
            }
        }

        double averageClean = bestSum / windowSize;

        OffsetDateTime startTime = slots.get(bestStart).from();
        OffsetDateTime endTime = startTime.plusHours(hours);

        return new OptimalChargingWindow(region, startTime, endTime, averageClean);
    }
}
