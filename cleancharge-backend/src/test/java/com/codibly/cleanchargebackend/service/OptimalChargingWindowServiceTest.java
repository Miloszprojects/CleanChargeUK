package com.codibly.cleanchargebackend.service;

import com.codibly.cleanchargebackend.domain.OptimalChargingWindow;
import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationMixDto;
import com.codibly.cleanchargebackend.repository.EnergyDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OptimalChargingWindowServiceTest {

    private EnergyDataRepository repository;
    private OptimalChargingWindowService service;

    @BeforeEach
    void setUp() {
        repository = mock(EnergyDataRepository.class);
        service = new OptimalChargingWindowService(repository);
    }

    @Test
    @DisplayName("findOptimalWindow: throws when hours outside [1..6]")
    void findOptimalWindow_throwsForInvalidHours() {
        assertThatThrownBy(() -> service.findOptimalWindow(0, RegionScope.NATIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hours must be between 1 and 6");

        assertThatThrownBy(() -> service.findOptimalWindow(7, RegionScope.NATIONAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hours must be between 1 and 6");
    }

    @Test
    @DisplayName("findOptimalWindow: uses national repository when region is NATIONAL")
    void findOptimalWindow_usesNationalRepo() {
        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(
                        slot("2025-01-01T00:00:00Z", 10.0),
                        slot("2025-01-01T00:30:00Z", 20.0)
                ));

        service.findOptimalWindow(1, RegionScope.NATIONAL);

        verify(repository, times(1)).getNationalGeneration(any(), any());
        verify(repository, never()).getRegionalGeneration(any(), any(), any());
    }

    @Test
    @DisplayName("findOptimalWindow: uses regional repository when region is not NATIONAL")
    void findOptimalWindow_usesRegionalRepo() {
        when(repository.getRegionalGeneration(eq(RegionScope.ENGLAND), any(), any()))
                .thenReturn(List.of(
                        slot("2025-01-01T00:00:00Z", 10.0),
                        slot("2025-01-01T00:30:00Z", 20.0)
                ));

        service.findOptimalWindow(1, RegionScope.ENGLAND);

        verify(repository, times(1)).getRegionalGeneration(eq(RegionScope.ENGLAND), any(), any());
        verify(repository, never()).getNationalGeneration(any(), any());
    }

    @Test
    @DisplayName("findOptimalWindow: calls repository with from truncated to hours and range of 48 hours")
    void findOptimalWindow_callsRepoWith48hRange_andTruncatedFrom() {
        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(
                        slot("2025-01-01T00:00:00Z", 10.0),
                        slot("2025-01-01T00:30:00Z", 20.0)
                ));

        service.findOptimalWindow(1, RegionScope.NATIONAL);

        ArgumentCaptor<Instant> fromCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCap   = ArgumentCaptor.forClass(Instant.class);

        verify(repository).getNationalGeneration(fromCap.capture(), toCap.capture());

        Instant from = fromCap.getValue();
        Instant to   = toCap.getValue();

        assertThat(from).isEqualTo(from.truncatedTo(java.time.temporal.ChronoUnit.HOURS));
        assertThat(Duration.between(from, to)).isEqualTo(Duration.ofHours(48));
    }

    @Test
    @DisplayName("findOptimalWindow: throws when not enough slots for requested window")
    void findOptimalWindow_throwsWhenNotEnoughData() {
        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(
                        slot("2025-01-01T00:00:00Z", 10.0),
                        slot("2025-01-01T00:30:00Z", 20.0),
                        slot("2025-01-01T01:00:00Z", 30.0)
                ));

        assertThatThrownBy(() -> service.findOptimalWindow(2, RegionScope.NATIONAL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough data for requested window");
    }

    @Test
    @DisplayName("findOptimalWindow: chooses window with highest average clean percentage and computes start/end")
    void findOptimalWindow_selectsBestWindow() {

        when(repository.getNationalGeneration(any(), any()))
                .thenReturn(List.of(
                        slot("2025-01-01T00:00:00Z", 10.0),
                        slot("2025-01-01T00:30:00Z", 10.0),
                        slot("2025-01-01T01:00:00Z", 10.0),
                        slot("2025-01-01T01:30:00Z", 10.0),
                        slot("2025-01-01T02:00:00Z", 90.0),
                        slot("2025-01-01T02:30:00Z", 90.0)
                ));

        OptimalChargingWindow result = service.findOptimalWindow(2, RegionScope.NATIONAL);

        OffsetDateTime expectedStart = OffsetDateTime.parse("2025-01-01T01:00:00Z");
        OffsetDateTime expectedEnd   = expectedStart.plusHours(2);

        assertThat(result.region()).isEqualTo(RegionScope.NATIONAL);
        assertThat(result.start()).isEqualTo(expectedStart);
        assertThat(result.end()).isEqualTo(expectedEnd);
        assertThat(result.averageCleanEnergyPercentage()).isEqualTo(50.0);
    }

    private static GenerationIntervalDto slot(String from, double cleanPerc) {
        OffsetDateTime start = OffsetDateTime.parse(from);
        OffsetDateTime end = start.plusMinutes(30);

        double dirty = 100.0 - cleanPerc;

        return new GenerationIntervalDto(
                start.toString(),
                end.toString(),
                List.of(
                        new GenerationMixDto("wind", cleanPerc),
                        new GenerationMixDto("gas", dirty)
                )
        );
    }
}
