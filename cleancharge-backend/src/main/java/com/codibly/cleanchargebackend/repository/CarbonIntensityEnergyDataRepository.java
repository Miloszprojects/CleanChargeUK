package com.codibly.cleanchargebackend.repository;

import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CarbonIntensityEnergyDataRepository implements EnergyDataRepository {

    private final RestTemplate restTemplate;

    @Value("${cleancharge.carbon-api.base-url}")
    private String baseUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
                    .withZone(ZoneOffset.UTC);

    @Override
    public List<GenerationIntervalDto> getNationalGeneration(Instant from, Instant to) {
        String fromStr = FORMATTER.format(from);
        String toStr   = FORMATTER.format(to);

        String url = baseUrl + "/generation/{from}/{to}";
        GenerationApiResponse response = restTemplate.getForObject(
                url, GenerationApiResponse.class, fromStr, toStr
        );

        return response != null ? response.data() : List.of();
    }

    @Override
    public List<GenerationIntervalDto> getRegionalGeneration(RegionScope region, Instant from, Instant to) {
        String path = switch (region) {
            case ENGLAND -> "england";
            case SCOTLAND -> "scotland";
            case WALES -> "wales";
            default -> throw new IllegalArgumentException("Region must be ENGLAND/SCOTLAND/WALES");
        };

        String fromStr = FORMATTER.format(from);
        String toStr   = FORMATTER.format(to);

        String url = baseUrl + "/regional/" + path + "/generation/{from}/{to}";
        RegionalGenerationApiResponse response = restTemplate.getForObject(
                url, RegionalGenerationApiResponse.class, fromStr, toStr
        );

        if (response == null) {
            return List.of();
        }

        return response.data().stream()
                .map(interval -> new GenerationIntervalDto(
                        interval.from(),
                        interval.to(),
                        interval.regions().get(0).generationmix()
                ))
                .toList();
    }

    @Override
    public List<GenerationIntervalDto> getCiRegionGeneration(int ciRegionId, Instant from, Instant to) {
        String fromStr = FORMATTER.format(from);
        String toStr   = FORMATTER.format(to);

        String url = baseUrl + "/regional/intensity/{from}/{to}/regionid/{regionid}";
        RegionIdApiResponse response = restTemplate.getForObject(
                url, RegionIdApiResponse.class, fromStr, toStr, ciRegionId
        );

        if (response == null || response.data() == null) {
            return List.of();
        }

        RegionIdItemDto item = response.data();

        return item.data().stream()
                .map(slot -> new GenerationIntervalDto(
                        slot.from(),
                        slot.to(),
                        slot.generationmix()
                ))
                .toList();
    }
}
