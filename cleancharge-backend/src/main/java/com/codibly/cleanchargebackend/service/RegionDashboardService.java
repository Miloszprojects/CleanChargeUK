package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.api.dto.ChargingWindowOptionDto;
import com.codibly.cleanchargebackend.api.dto.DailyEnergyMixDto;
import com.codibly.cleanchargebackend.api.dto.FuelShareDto;
import com.codibly.cleanchargebackend.api.dto.RegionDashboardSummaryDto;
import com.codibly.cleanchargebackend.domain.CiRegion;
import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.GenerationSlot;
import com.codibly.cleanchargebackend.domain.mapper.GenerationSlotMapper;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RegionDashboardService {

    private final EnergyDataRepository repository;
    private static final ZoneId UK = ZoneId.of("Europe/London");

    public RegionDashboardSummaryDto getRegionSummary(int regionId) {
        CiRegion region = CiRegion.fromId(regionId);

        Instant from = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant to = from.plus(48, ChronoUnit.HOURS);

        List<GenerationIntervalDto> intervals =
                repository.getCiRegionGeneration(region.getId(), from, to);

        if (intervals.isEmpty()) {
            throw new IllegalStateException("No data for region " + regionId);
        }

        GenerationIntervalDto current = chooseCurrentInterval(intervals);
        List<FuelShareDto> currentMix = current.generationmix().stream()
                .map(mix -> new FuelShareDto(mix.fuel(), mix.perc()))
                .toList();

        List<ChargingWindowOptionDto> windows = IntStream.rangeClosed(1, 6)
                .mapToObj(hours -> computeBestWindow(hours, intervals))
                .toList();

        List<DailyEnergyMixDto> dailyMixes = computeDailyMixes(intervals);

        return new RegionDashboardSummaryDto(
                region.getId(),
                region.getShortName(),
                currentMix,
                windows,
                dailyMixes
        );
    }

    private GenerationIntervalDto chooseCurrentInterval(List<GenerationIntervalDto> intervals) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return intervals.stream()
                .filter(i -> {
                    OffsetDateTime from = OffsetDateTime.parse(i.from());
                    OffsetDateTime to = OffsetDateTime.parse(i.to());
                    return !now.isBefore(from) && now.isBefore(to);
                })
                .findFirst()
                .orElse(intervals.get(0));
    }

    private ChargingWindowOptionDto computeBestWindow(int hours, List<GenerationIntervalDto> intervals) {
        if (hours < 1 || hours > 6) {
            throw new IllegalArgumentException("hours must be between 1 and 6");
        }

        List<GenerationSlot> allSlots = GenerationSlotMapper.toDomain(intervals);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<GenerationSlot> slots = allSlots.stream()
                .filter(s -> !s.from().isBefore(now))
                .toList();

        int slotCount = slots.size();
        int windowSize = hours * 2; // 2 × 30 min

        if (slotCount < windowSize) {
            throw new IllegalStateException("Not enough data for requested window");
        }

        double[] cleanPerc = new double[slotCount];
        for (int i = 0; i < slotCount; i++) {
            GenerationSlot slot = slots.get(i);
            double sum = slot.fuelShare().entrySet().stream()
                    .filter(e -> e.getKey().isClean())
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
            cleanPerc[i] = sum;
        }

        double currentSum = 0.0;
        for (int i = 0; i < windowSize; i++) currentSum += cleanPerc[i];

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

        return new ChargingWindowOptionDto(
                hours,
                startTime,
                endTime,
                averageClean
        );
    }

    private List<DailyEnergyMixDto> computeDailyMixes(List<GenerationIntervalDto> intervals) {
        List<GenerationSlot> slots = GenerationSlotMapper.toDomain(intervals);

        Map<LocalDate, List<GenerationSlot>> byDayUk = new HashMap<>();
        for (GenerationSlot s : slots) {
            LocalDate dayUk = s.from().atZoneSameInstant(UK).toLocalDate();
            byDayUk.computeIfAbsent(dayUk, k -> new ArrayList<>()).add(s);
        }

        LocalDate todayUk = LocalDate.now(UK);
        List<LocalDate> wantedDays = List.of(todayUk, todayUk.plusDays(1), todayUk.plusDays(2));

        List<DailyEnergyMixDto> result = new ArrayList<>();

        for (LocalDate day : wantedDays) {
            List<GenerationSlot> daySlots = byDayUk.getOrDefault(day, List.of());

            if (daySlots.isEmpty()) {
                result.add(new DailyEnergyMixDto(day, 0.0, List.of()));
                continue;
            }

            int n = daySlots.size();
            Map<String, Double> totals = new HashMap<>();

            for (GenerationSlot slot : daySlots) {
                for (var e : slot.fuelShare().entrySet()) {
                    FuelType fuelType = e.getKey();
                    double perc = e.getValue();

                    String fuelKey = fuelType.name().toLowerCase(Locale.ROOT);

                    totals.merge(fuelKey, perc, Double::sum);
                }
            }

            List<FuelShareDto> mix = totals.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> new FuelShareDto(e.getKey(), e.getValue() / n))
                    .toList();

            double clean = daySlots.stream()
                    .mapToDouble(slot -> slot.fuelShare().entrySet().stream()
                            .filter(x -> x.getKey().isClean())
                            .mapToDouble(Map.Entry::getValue)
                            .sum()
                    )
                    .average()
                    .orElse(0.0);

            result.add(new DailyEnergyMixDto(day, clean, mix));
        }

        return result;
    }
}
