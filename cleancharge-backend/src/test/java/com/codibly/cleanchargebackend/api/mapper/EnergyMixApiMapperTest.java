package com.codibly.cleanchargebackend.api.mapper;

import com.codibly.cleanchargebackend.api.dto.*;
import com.codibly.cleanchargebackend.domain.DailyEnergyMixSummary;
import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.RegionScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class EnergyMixApiMapperTest {

    @Test
    @DisplayName("toResponse: maps list of DailyEnergyMixSummary into EnergyMixResponseDto (mix order not guaranteed)")
    void toResponse_mapsAllDays() {
        Map<FuelType, Double> mixDay1 = new EnumMap<>(FuelType.class);
        mixDay1.put(FuelType.fromApi("wind"), 70.0);
        mixDay1.put(FuelType.fromApi("gas"), 30.0);

        Map<FuelType, Double> mixDay2 = new EnumMap<>(FuelType.class);
        mixDay2.put(FuelType.fromApi("solar"), 20.0);
        mixDay2.put(FuelType.fromApi("gas"), 80.0);

        DailyEnergyMixSummary s1 = new DailyEnergyMixSummary(
                LocalDate.parse("2025-01-01"),
                RegionScope.NATIONAL,
                mixDay1,
                70.0
        );

        DailyEnergyMixSummary s2 = new DailyEnergyMixSummary(
                LocalDate.parse("2025-01-02"),
                RegionScope.ENGLAND,
                mixDay2,
                20.0
        );

        EnergyMixResponseDto result = EnergyMixApiMapper.toResponse(List.of(s1, s2));

        assertThat(result.days()).hasSize(2);

        EnergyMixDayDto day1 = result.days().get(0);
        assertThat(day1.date()).isEqualTo(LocalDate.parse("2025-01-01"));
        assertThat(day1.region()).isEqualTo("NATIONAL");
        assertThat(day1.cleanEnergyPercentage()).isEqualTo(70.0);
        assertThat(day1.mix()).containsExactlyInAnyOrder(
                new FuelShareDto("wind", 70.0),
                new FuelShareDto("gas", 30.0)
        );

        EnergyMixDayDto day2 = result.days().get(1);
        assertThat(day2.date()).isEqualTo(LocalDate.parse("2025-01-02"));
        assertThat(day2.region()).isEqualTo("ENGLAND");
        assertThat(day2.cleanEnergyPercentage()).isEqualTo(20.0);
        assertThat(day2.mix()).containsExactlyInAnyOrder(
                new FuelShareDto("solar", 20.0),
                new FuelShareDto("gas", 80.0)
        );
    }

    @Test
    @DisplayName("toResponse: throws NullPointerException when summaries is null (fail fast)")
    void toResponse_throwsWhenSummariesNull() {
        assertThatThrownBy(() -> EnergyMixApiMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
    }
}
