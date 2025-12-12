package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.api.dto.RegionDashboardSummaryDto;
import com.codibly.cleanchargebackend.domain.CiRegion;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationMixDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegionDashboardServiceTest {

    private static final ZoneId UK = ZoneId.of("Europe/London");

    private EnergyDataRepository repository;
    private RegionDashboardService service;

    @BeforeEach
    void setUp() {
        repository = mock(EnergyDataRepository.class);
        service = new RegionDashboardService(repository);
    }

    @Test
    @DisplayName("getRegionSummary: throws when repository returns empty intervals")
    void getRegionSummary_throwsWhenNoData() {
        CiRegion region = CiRegion.values()[0];
        when(repository.getCiRegionGeneration(eq(region.getId()), any(), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.getRegionSummary(region.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No data for region");
    }

    @Test
    @DisplayName("getRegionSummary: calls repository with from truncated to hours and to = from + 48h")
    void getRegionSummary_callsRepoWith48hRange() {
        CiRegion region = CiRegion.values()[0];
        when(repository.getCiRegionGeneration(eq(region.getId()), any(), any()))
                .thenReturn(minimalIntervalsCoveringNowPlusFuture());

        service.getRegionSummary(region.getId());

        ArgumentCaptor<Instant> fromCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCap   = ArgumentCaptor.forClass(Instant.class);

        verify(repository).getCiRegionGeneration(eq(region.getId()), fromCap.capture(), toCap.capture());

        Instant from = fromCap.getValue();
        Instant to   = toCap.getValue();

        assertThat(from).isEqualTo(from.truncatedTo(ChronoUnit.HOURS));
        assertThat(Duration.between(from, to)).isEqualTo(Duration.ofHours(48));
    }

    @Test
    @DisplayName("getRegionSummary: sets name from CiRegion and currentMix from chosen current interval")
    void getRegionSummary_setsNameAndCurrentMix() {
        CiRegion region = CiRegion.values()[0];

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        GenerationIntervalDto current = interval(
                now.minusMinutes(5),
                now.plusMinutes(25),
                new GenerationMixDto("wind", 70.0),
                new GenerationMixDto("gas", 30.0)
        );

        List<GenerationIntervalDto> future = futureHalfHourSlots(now.plusMinutes(1), 20);

        List<GenerationIntervalDto> intervals = new ArrayList<>();
        intervals.add(current);
        intervals.addAll(future);

        when(repository.getCiRegionGeneration(eq(region.getId()), any(), any()))
                .thenReturn(intervals);

        RegionDashboardSummaryDto result = service.getRegionSummary(region.getId());

        assertThat(result.regionId()).isEqualTo(region.getId());
        assertThat(result.name()).isEqualTo(region.getShortName());

        assertThat(result.currentMix())
                .extracting(x -> x.fuel() + ":" + x.percentage())
                .containsExactlyInAnyOrder("wind:70.0", "gas:30.0");
    }

    @Test
    @DisplayName("getRegionSummary: returns 6 charging windows for hours 1..6 with correct duration")
    void getRegionSummary_returnsChargingWindows1to6() {
        CiRegion region = CiRegion.values()[0];

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<GenerationIntervalDto> intervals = minimalIntervalsCoveringNowPlusFuture();

        when(repository.getCiRegionGeneration(eq(region.getId()), any(), any()))
                .thenReturn(intervals);

        RegionDashboardSummaryDto result = service.getRegionSummary(region.getId());

        assertThat(result.chargingWindows()).hasSize(6);

        assertThat(result.chargingWindows())
                .extracting(w -> w.hours())
                .containsExactly(1, 2, 3, 4, 5, 6);

        assertThat(result.chargingWindows()).allSatisfy(w -> {
            long h = Duration.between(w.start(), w.end()).toHours();
            assertThat(h).isEqualTo(w.hours());
        });

        assertThat(result.chargingWindows()).allSatisfy(w ->
                assertThat(w.averageCleanEnergyPercentage()).isBetween(0.0, 100.0)
        );
    }

    @Test
    @DisplayName("getRegionSummary: returns dailyMixes for UK Today/Tomorrow/DayAfter")
    void getRegionSummary_returnsThreeUkDays() {
        CiRegion region = CiRegion.values()[0];

        when(repository.getCiRegionGeneration(eq(region.getId()), any(), any()))
                .thenReturn(minimalIntervalsCoveringNowPlusFuture());

        LocalDate todayUk = LocalDate.now(UK);

        RegionDashboardSummaryDto result = service.getRegionSummary(region.getId());

        assertThat(result.dailyMixes()).hasSize(3);
        assertThat(result.dailyMixes())
                .extracting(d -> d.date())
                .containsExactly(todayUk, todayUk.plusDays(1), todayUk.plusDays(2));
    }

    private static List<GenerationIntervalDto> minimalIntervalsCoveringNowPlusFuture() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        GenerationIntervalDto current = interval(
                now.minusMinutes(5),
                now.plusMinutes(25),
                new GenerationMixDto("wind", 60.0),
                new GenerationMixDto("gas", 40.0)
        );

        List<GenerationIntervalDto> future = futureHalfHourSlots(now.plusMinutes(1), 20);

        List<GenerationIntervalDto> all = new ArrayList<>();
        all.add(current);
        all.addAll(future);
        return all;
    }

    private static List<GenerationIntervalDto> futureHalfHourSlots(OffsetDateTime start, int n) {
        List<GenerationIntervalDto> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            OffsetDateTime from = start.plusMinutes(30L * i);
            OffsetDateTime to   = from.plusMinutes(30);

            double clean = Math.min(100.0, 10.0 + i * 2.0);
            double dirty = 100.0 - clean;

            list.add(interval(
                    from,
                    to,
                    new GenerationMixDto("wind", clean),
                    new GenerationMixDto("gas", dirty)
            ));
        }
        return list;
    }

    private static GenerationIntervalDto interval(OffsetDateTime from, OffsetDateTime to, GenerationMixDto... mix) {
        return new GenerationIntervalDto(from.toString(), to.toString(), List.of(mix));
    }
}
