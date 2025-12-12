package com.codibly.cleanchargebackend.domain.mapper;

import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.GenerationSlot;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationIntervalDto;
import com.codibly.cleanchargebackend.external.carbonintensity.dto.GenerationMixDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GenerationSlotMapperTest {

    @Test
    @DisplayName("toDomain: maps from/to and generationmix into GenerationSlot.fuelShare")
    void toDomain_mapsSingleDto() {
        GenerationIntervalDto dto = new GenerationIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                List.of(
                        new GenerationMixDto("gas", 12.3),
                        new GenerationMixDto("wind", 45.0),
                        new GenerationMixDto("solar", 3.7)
                )
        );

        GenerationSlot result = GenerationSlotMapper.toDomain(dto);

        assertThat(result.from()).isEqualTo(OffsetDateTime.parse("2025-01-01T10:00:00Z"));
        assertThat(result.to()).isEqualTo(OffsetDateTime.parse("2025-01-01T10:30:00Z"));

        assertThat(result.fuelShare())
                .hasSize(3)
                .containsEntry(FuelType.fromApi("gas"), 12.3)
                .containsEntry(FuelType.fromApi("wind"), 45.0)
                .containsEntry(FuelType.fromApi("solar"), 3.7);
    }

    @Test
    @DisplayName("toDomain: when same fuel occurs twice, last value wins (Map.put overwrite)")
    void toDomain_overwritesDuplicateFuel() {
        GenerationIntervalDto dto = new GenerationIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                List.of(
                        new GenerationMixDto("gas", 10.0),
                        new GenerationMixDto("gas", 20.0)
                )
        );

        GenerationSlot result = GenerationSlotMapper.toDomain(dto);

        assertThat(result.fuelShare())
                .hasSize(1)
                .containsEntry(FuelType.fromApi("gas"), 20.0);
    }

    @Test
    @DisplayName("toDomain(list): maps all intervals preserving order")
    void toDomain_list_mapsAll() {
        GenerationIntervalDto dto1 = new GenerationIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                List.of(new GenerationMixDto("gas", 10.0))
        );
        GenerationIntervalDto dto2 = new GenerationIntervalDto(
                "2025-01-01T10:30:00Z",
                "2025-01-01T11:00:00Z",
                List.of(new GenerationMixDto("wind", 99.0))
        );

        List<GenerationSlot> result = GenerationSlotMapper.toDomain(List.of(dto1, dto2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).from()).isEqualTo(OffsetDateTime.parse("2025-01-01T10:00:00Z"));
        assertThat(result.get(1).from()).isEqualTo(OffsetDateTime.parse("2025-01-01T10:30:00Z"));
    }

    @Test
    @DisplayName("toDomain: throws when 'from' is invalid ISO-8601 offset date-time")
    void toDomain_throwsWhenFromInvalid() {
        GenerationIntervalDto dto = new GenerationIntervalDto(
                "invalid-date",
                "2025-01-01T10:30:00Z",
                List.of(new GenerationMixDto("gas", 10.0))
        );

        assertThatThrownBy(() -> GenerationSlotMapper.toDomain(dto))
                .isInstanceOf(RuntimeException.class); // DateTimeParseException (runtime)
    }

    @Test
    @DisplayName("toDomain: throws when 'to' is invalid ISO-8601 offset date-time")
    void toDomain_throwsWhenToInvalid() {
        GenerationIntervalDto dto = new GenerationIntervalDto(
                "2025-01-01T10:00:00Z",
                "invalid-date",
                List.of(new GenerationMixDto("gas", 10.0))
        );

        assertThatThrownBy(() -> GenerationSlotMapper.toDomain(dto))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("toDomain: throws when fuel is unknown (delegates to FuelType.fromApi)")
    void toDomain_throwsWhenFuelUnknown() {
        GenerationIntervalDto dto = new GenerationIntervalDto(
                "2025-01-01T10:00:00Z",
                "2025-01-01T10:30:00Z",
                List.of(new GenerationMixDto("unknown-fuel", 1.0))
        );

        assertThatThrownBy(() -> GenerationSlotMapper.toDomain(dto))
                .isInstanceOf(RuntimeException.class);
    }
}
