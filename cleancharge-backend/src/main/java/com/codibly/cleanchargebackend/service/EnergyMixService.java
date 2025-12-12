package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.domain.*;
import com.codibly.cleanchargebackend.domain.mapper.GenerationSlotMapper;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnergyMixService {

    private final EnergyDataRepository repository;

    public List<DailyEnergyMixSummary> getThreeDayMix() {

        Instant from = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = LocalDate.now(ZoneOffset.UTC).plusDays(3)
                .atStartOfDay().toInstant(ZoneOffset.UTC);

        List<GenerationIntervalDto> intervals =
                repository.getNationalGeneration(from, to);

        var slots = GenerationSlotMapper.toDomain(intervals);

        Map<LocalDate, List<GenerationSlot>> byDay = slots.stream()
                .collect(Collectors.groupingBy(slot ->
                        slot.from().toLocalDate()
                ));

        List<DailyEnergyMixSummary> result = new ArrayList<>();

        for (var entry : byDay.entrySet()) {
            LocalDate day = entry.getKey();
            List<GenerationSlot> daySlots = entry.getValue();

            Map<FuelType, Double> totals = new EnumMap<>(FuelType.class);
            int n = daySlots.size();

            for (GenerationSlot slot : daySlots) {
                for (var e : slot.fuelShare().entrySet()) {
                    totals.merge(e.getKey(), e.getValue(), Double::sum);
                }
            }

            Map<FuelType, Double> averages = new EnumMap<>(FuelType.class);
            for (var e : totals.entrySet()) {
                averages.put(e.getKey(), e.getValue() / n);
            }

            double clean = averages.entrySet().stream()
                    .filter(e -> e.getKey().isClean())
                    .mapToDouble(Map.Entry::getValue)
                    .sum();

            result.add(new DailyEnergyMixSummary(day, RegionScope.NATIONAL, averages, clean));
        }

        return result.stream()
                .sorted(Comparator.comparing(DailyEnergyMixSummary::date))
                .limit(3)
                .toList();
    }
}
