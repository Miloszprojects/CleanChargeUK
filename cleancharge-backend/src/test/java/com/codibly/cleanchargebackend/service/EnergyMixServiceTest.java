package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.domain.DailyEnergyMixSummary;
import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationMixDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnergyMixServiceTest {

    private EnergyDataRepository repository;
    private EnergyMixService service;

    @BeforeEach
    void setUp() {
        repository = mock(EnergyDataRepository.class);
        service = new EnergyMixService(repository);
    }

    @Test
    @DisplayName("getThreeDayMix: calls repository with UTC midnight range [today, today+3days)")
    void getThreeDayMix_callsRepositoryWithUtcMidnightRange() {
        when(repository.getNationalGeneration(any(), any())).thenReturn(List.of());

        service.getThreeDayMix();

        ArgumentCaptor<Instant> fromCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCap   = ArgumentCaptor.forClass(Instant.class);

        verify(repository).getNationalGeneration(fromCap.capture(), toCap.capture());

        Instant from = fromCap.getValue();
        Instant to   = toCap.getValue();

        assertThat(from.atOffset(ZoneOffset.UTC).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(to.atOffset(ZoneOffset.UTC).toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        assertThat(Duration.between(from, to).toDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("getThreeDayMix: groups by day, computes average mix and clean value, returns sorted")
    void getThreeDayMix_computesDailySummaries() {
        GenerationIntervalDto d1s1 = interval("2025-01-01T00:00:00Z", "2025-01-01T00:30:00Z",
                mix("gas", 40.0), mix("wind", 60.0));
        GenerationIntervalDto d1s2 = interval("2025-01-01T00:30:00Z", "2025-01-01T01:00:00Z",
                mix("gas", 20.0), mix("wind", 80.0));

        GenerationIntervalDto d2s1 = interval("2025-01-02T00:00:00Z", "2025-01-02T00:30:00Z",
                mix("gas", 10.0), mix("wind", 90.0));

        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(d1s1, d1s2, d2s1));

        Map<FuelType, Double> day1Avg = new EnumMap<>(FuelType.class);
        day1Avg.put(FuelType.fromApi("gas"), 30.0);
        day1Avg.put(FuelType.fromApi("wind"), 70.0);

        Map<FuelType, Double> day2Avg = new EnumMap<>(FuelType.class);
        day2Avg.put(FuelType.fromApi("gas"), 10.0);
        day2Avg.put(FuelType.fromApi("wind"), 90.0);

        DailyEnergyMixSummary expectedDay1 =
                new DailyEnergyMixSummary(LocalDate.parse("2025-01-01"), RegionScope.NATIONAL, day1Avg, 70.0);

        DailyEnergyMixSummary expectedDay2 =
                new DailyEnergyMixSummary(LocalDate.parse("2025-01-02"), RegionScope.NATIONAL, day2Avg, 90.0);

        List<DailyEnergyMixSummary> result = service.getThreeDayMix();

        assertThat(result)
                .containsExactly(expectedDay1, expectedDay2);
    }

    @Test
    @DisplayName("getThreeDayMix: returns max 3 days (sorted by date)")
    void getThreeDayMix_limitsToThreeDays() {
        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(
                        interval("2025-01-01T00:00:00Z", "2025-01-01T00:30:00Z", mix("wind", 100.0)),
                        interval("2025-01-02T00:00:00Z", "2025-01-02T00:30:00Z", mix("wind", 100.0)),
                        interval("2025-01-03T00:00:00Z", "2025-01-03T00:30:00Z", mix("wind", 100.0)),
                        interval("2025-01-04T00:00:00Z", "2025-01-04T00:30:00Z", mix("wind", 100.0))
                ));

        Map<FuelType, Double> avg = new EnumMap<>(FuelType.class);
        avg.put(FuelType.fromApi("wind"), 100.0);

        DailyEnergyMixSummary d1 = new DailyEnergyMixSummary(LocalDate.parse("2025-01-01"), RegionScope.NATIONAL, avg, 100.0);
        DailyEnergyMixSummary d2 = new DailyEnergyMixSummary(LocalDate.parse("2025-01-02"), RegionScope.NATIONAL, avg, 100.0);
        DailyEnergyMixSummary d3 = new DailyEnergyMixSummary(LocalDate.parse("2025-01-03"), RegionScope.NATIONAL, avg, 100.0);

        List<DailyEnergyMixSummary> result = service.getThreeDayMix();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(d1, d2, d3);
    }

    private static GenerationIntervalDto interval(String from, String to, GenerationMixDto... mix) {
        return new GenerationIntervalDto(from, to, List.of(mix));
    }

    private static GenerationMixDto mix(String fuel, double perc) {
        return new GenerationMixDto(fuel, perc);
    }
}
