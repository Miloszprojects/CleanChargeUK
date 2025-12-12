package com.codibly.cleanchargebackend.api.mapper;

import com.codibly.cleanchargebackend.api.dto.OptimalChargingWindowResponseDto;
import com.codibly.cleanchargebackend.domain.OptimalChargingWindow;
import com.codibly.cleanchargebackend.domain.RegionScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

class ChargingWindowApiMapperTest {

    @Test
    @DisplayName("toResponse: maps OptimalChargingWindow to OptimalChargingWindowResponseDto")
    void toResponse_mapsAllFields() {
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T10:00:00Z");
        OffsetDateTime end   = OffsetDateTime.parse("2025-01-01T12:00:00Z");

        OptimalChargingWindow window = new OptimalChargingWindow(
                RegionScope.ENGLAND,
                start,
                end,
                55.5
        );

        OptimalChargingWindowResponseDto dto = ChargingWindowApiMapper.toResponse(window);

        assertThat(dto).isEqualTo(new OptimalChargingWindowResponseDto(
                "ENGLAND",
                start,
                end,
                55.5
        ));
    }

    @Test
    @DisplayName("toResponse: throws NullPointerException when window is null (fail fast)")
    void toResponse_throwsWhenWindowIsNull() {
        assertThatThrownBy(() -> ChargingWindowApiMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
