package com.codibly.cleanchargebackend.repository;

import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CarbonIntensityEnergyDataRepositoryTest {

    private RestTemplate restTemplate;
    private CarbonIntensityEnergyDataRepository repository;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        repository = new CarbonIntensityEnergyDataRepository(restTemplate);
        ReflectionTestUtils.setField(repository, "baseUrl", "http://carbon");
    }

    @Test
    @DisplayName("getNationalGeneration: returns response.data when API returns non-null response")
    void getNationalGeneration_returnsData() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        List<GenerationIntervalDto> apiData = List.of(
                new GenerationIntervalDto(
                        "2025-01-01T10:00:00Z",
                        "2025-01-01T10:30:00Z",
                        List.of(new GenerationMixDto("gas", 10.0))
                )
        );

        GenerationApiResponse response = new GenerationApiResponse(apiData);

        when(restTemplate.getForObject(
                eq("http://carbon/generation/{from}/{to}"),
                eq(GenerationApiResponse.class),
                anyString(),
                anyString()
        )).thenReturn(response);

        List<GenerationIntervalDto> result = repository.getNationalGeneration(from, to);

        assertThat(result).isEqualTo(apiData);

        verify(restTemplate).getForObject(
                eq("http://carbon/generation/{from}/{to}"),
                eq(GenerationApiResponse.class),
                eq("2025-01-01T10:00Z"),
                eq("2025-01-01T10:30Z")
        );
    }

    @Test
    @DisplayName("getNationalGeneration: returns empty list when API returns null response")
    void getNationalGeneration_returnsEmpty_whenResponseNull() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        when(restTemplate.getForObject(
                anyString(),
                eq(GenerationApiResponse.class),
                anyString(),
                anyString()
        )).thenReturn(null);

        List<GenerationIntervalDto> result = repository.getNationalGeneration(from, to);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "getRegionalGeneration: region {0} -> path {1}")
    @CsvSource({
            "ENGLAND, england",
            "SCOTLAND, scotland",
            "WALES, wales"
    })
    void getRegionalGeneration_usesCorrectPath_andMapsFirstRegionMix(RegionScope region, String expectedPath) {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        List<GenerationMixDto> mix = List.of(new GenerationMixDto("wind", 55.5));

        RegionDto regionDto = new RegionDto(
                123,
                "DNO",
                "short",
                new RegionalIntensityDto(1, 2, "low"),
                mix
        );

        RegionalIntervalDto interval = new RegionalIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                List.of(regionDto)
        );

        RegionalGenerationApiResponse response = new RegionalGenerationApiResponse(List.of(interval));

        when(restTemplate.getForObject(
                eq("http://carbon/regional/" + expectedPath + "/generation/{from}/{to}"),
                eq(RegionalGenerationApiResponse.class),
                anyString(),
                anyString()
        )).thenReturn(response);

        List<GenerationIntervalDto> result = repository.getRegionalGeneration(region, from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).from()).isEqualTo("2025-01-01T10:00:00Z");
        assertThat(result.get(0).to()).isEqualTo("2025-01-01T10:30:00Z");
        assertThat(result.get(0).generationmix()).isEqualTo(mix);

        verify(restTemplate).getForObject(
                eq("http://carbon/regional/" + expectedPath + "/generation/{from}/{to}"),
                eq(RegionalGenerationApiResponse.class),
                eq("2025-01-01T10:00Z"),
                eq("2025-01-01T10:30Z")
        );
    }

    @Test
    @DisplayName("getRegionalGeneration: returns empty list when API returns null response")
    void getRegionalGeneration_returnsEmpty_whenResponseNull() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        when(restTemplate.getForObject(
                anyString(),
                eq(RegionalGenerationApiResponse.class),
                anyString(),
                anyString()
        )).thenReturn(null);

        List<GenerationIntervalDto> result = repository.getRegionalGeneration(RegionScope.ENGLAND, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRegionalGeneration: throws IllegalArgumentException for unsupported region")
    void getRegionalGeneration_throwsForUnsupportedRegion() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        assertThatThrownBy(() -> repository.getRegionalGeneration(RegionScope.NATIONAL, from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Region must be ENGLAND/SCOTLAND/WALES");
    }

    @Test
    @DisplayName("getCiRegionGeneration: returns empty when response is null")
    void getCiRegionGeneration_returnsEmpty_whenResponseNull() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        when(restTemplate.getForObject(
                anyString(),
                eq(RegionIdApiResponse.class),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(null);

        List<GenerationIntervalDto> result = repository.getCiRegionGeneration(1, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCiRegionGeneration: returns empty when response.data is null")
    void getCiRegionGeneration_returnsEmpty_whenDataNull() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        RegionIdApiResponse response = new RegionIdApiResponse(null);

        when(restTemplate.getForObject(
                anyString(),
                eq(RegionIdApiResponse.class),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(response);

        List<GenerationIntervalDto> result = repository.getCiRegionGeneration(1, from, to);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCiRegionGeneration: maps item.data intervals into GenerationIntervalDto list")
    void getCiRegionGeneration_mapsIntervals() {
        Instant from = Instant.parse("2025-01-01T10:00:00Z");
        Instant to   = Instant.parse("2025-01-01T10:30:00Z");

        List<GenerationMixDto> mix = List.of(new GenerationMixDto("gas", 11.1));

        RegionIdIntervalDto interval = new RegionIdIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                new RegionalIntensityDto(1, 2, "low"),
                mix
        );

        RegionIdItemDto item = new RegionIdItemDto(
                42,
                "DNO",
                "short",
                List.of(interval)
        );

        RegionIdApiResponse response = new RegionIdApiResponse(item);

        when(restTemplate.getForObject(
                eq("http://carbon/regional/intensity/{from}/{to}/regionid/{regionid}"),
                eq(RegionIdApiResponse.class),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(response);

        List<GenerationIntervalDto> result = repository.getCiRegionGeneration(42, from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).from()).isEqualTo("2025-01-01T10:00:00Z");
        assertThat(result.get(0).to()).isEqualTo("2025-01-01T10:30:00Z");
        assertThat(result.get(0).generationmix()).isEqualTo(mix);

        verify(restTemplate).getForObject(
                eq("http://carbon/regional/intensity/{from}/{to}/regionid/{regionid}"),
                eq(RegionIdApiResponse.class),
                eq("2025-01-01T10:00Z"),
                eq("2025-01-01T10:30Z"),
                eq(42)
        );
    }
}
