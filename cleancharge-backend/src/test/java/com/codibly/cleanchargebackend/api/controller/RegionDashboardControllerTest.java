package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.api.dto.*;
import com.codibly.cleanchargebackend.service.RegionDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegionDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegionDashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegionDashboardService regionDashboardService;

    @Test
    @DisplayName("GET /api/regions/{regionId}/summary: returns RegionDashboardSummaryDto JSON")
    void getRegionSummary_returnsDto() throws Exception {
        // given
        int regionId = 42;

        RegionDashboardSummaryDto dto = new RegionDashboardSummaryDto(
                regionId,
                "London",
                List.of(
                        new FuelShareDto("wind", 60.0),
                        new FuelShareDto("gas", 40.0)
                ),
                List.of(
                        new ChargingWindowOptionDto(
                                2,
                                OffsetDateTime.parse("2025-01-01T10:00:00Z"),
                                OffsetDateTime.parse("2025-01-01T12:00:00Z"),
                                55.5
                        )
                ),
                List.of(
                        new DailyEnergyMixDto(
                                LocalDate.parse("2025-01-01"),
                                70.0,
                                List.of(
                                        new FuelShareDto("wind", 70.0),
                                        new FuelShareDto("gas", 30.0)
                                )
                        )
                )
        );

        when(regionDashboardService.getRegionSummary(regionId)).thenReturn(dto);

        // when / then
        mockMvc.perform(get("/api/regions/{regionId}/summary", regionId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))

                .andExpect(jsonPath("$.regionId").value(42))
                .andExpect(jsonPath("$.name").value("London"))

                // currentMix
                .andExpect(jsonPath("$.currentMix").isArray())
                .andExpect(jsonPath("$.currentMix.length()").value(2))
                .andExpect(jsonPath("$.currentMix[?(@.fuel=='wind')].percentage").exists())
                .andExpect(jsonPath("$.currentMix[?(@.fuel=='gas')].percentage").exists())

                // chargingWindows
                .andExpect(jsonPath("$.chargingWindows").isArray())
                .andExpect(jsonPath("$.chargingWindows.length()").value(1))
                .andExpect(jsonPath("$.chargingWindows[0].hours").value(2))
                .andExpect(jsonPath("$.chargingWindows[0].start").exists())
                .andExpect(jsonPath("$.chargingWindows[0].end").exists())
                .andExpect(jsonPath("$.chargingWindows[0].averageCleanEnergyPercentage").value(55.5))

                // dailyMixes
                .andExpect(jsonPath("$.dailyMixes").isArray())
                .andExpect(jsonPath("$.dailyMixes.length()").value(1))
                .andExpect(jsonPath("$.dailyMixes[0].date").value("2025-01-01"))
                .andExpect(jsonPath("$.dailyMixes[0].cleanPercentage").value(70.0))
                .andExpect(jsonPath("$.dailyMixes[0].mix").isArray());

        verify(regionDashboardService).getRegionSummary(42);
        verifyNoMoreInteractions(regionDashboardService);
    }
}
