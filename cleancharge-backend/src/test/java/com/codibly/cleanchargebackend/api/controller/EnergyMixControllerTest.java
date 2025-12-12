package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.domain.DailyEnergyMixSummary;
import com.codibly.cleanchargebackend.domain.FuelType;
import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.service.EnergyMixService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnergyMixController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnergyMixControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EnergyMixService energyMixService;

    @Test
    @DisplayName("GET /api/energy-mix: returns mapped EnergyMixResponseDto JSON")
    void getEnergyMix_returnsResponse() throws Exception {
        // given
        Map<FuelType, Double> mix1 = new EnumMap<>(FuelType.class);
        mix1.put(FuelType.fromApi("gas"), 30.0);
        mix1.put(FuelType.fromApi("wind"), 70.0);

        Map<FuelType, Double> mix2 = new EnumMap<>(FuelType.class);
        mix2.put(FuelType.fromApi("gas"), 80.0);
        mix2.put(FuelType.fromApi("solar"), 20.0);

        DailyEnergyMixSummary s1 = new DailyEnergyMixSummary(
                LocalDate.parse("2025-01-01"),
                RegionScope.NATIONAL,
                mix1,
                70.0
        );

        DailyEnergyMixSummary s2 = new DailyEnergyMixSummary(
                LocalDate.parse("2025-01-02"),
                RegionScope.ENGLAND,
                mix2,
                20.0
        );

        when(energyMixService.getThreeDayMix()).thenReturn(List.of(s1, s2));

        // when / then
        mockMvc.perform(get("/api/energy-mix"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))

                // days[0]
                .andExpect(jsonPath("$.days[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$.days[0].region").value("NATIONAL"))
                .andExpect(jsonPath("$.days[0].cleanEnergyPercentage").value(70.0))
                // mix order is NOT guaranteed -> check presence, not order
                .andExpect(jsonPath("$.days[0].mix[?(@.fuel=='gas')].percentage").value(30.0))
                .andExpect(jsonPath("$.days[0].mix[?(@.fuel=='wind')].percentage").value(70.0))

                // days[1]
                .andExpect(jsonPath("$.days[1].date").value("2025-01-02"))
                .andExpect(jsonPath("$.days[1].region").value("ENGLAND"))
                .andExpect(jsonPath("$.days[1].cleanEnergyPercentage").value(20.0))
                .andExpect(jsonPath("$.days[1].mix[?(@.fuel=='gas')].percentage").value(80.0))
                .andExpect(jsonPath("$.days[1].mix[?(@.fuel=='solar')].percentage").value(20.0));

        verify(energyMixService).getThreeDayMix();
        verifyNoMoreInteractions(energyMixService);
    }
}
