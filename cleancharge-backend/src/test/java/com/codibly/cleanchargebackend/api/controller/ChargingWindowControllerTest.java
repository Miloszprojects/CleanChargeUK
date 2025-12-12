package com.codibly.cleanchargebackend.api.controller;

import com.codibly.cleanchargebackend.domain.OptimalChargingWindow;
import com.codibly.cleanchargebackend.domain.RegionScope;
import com.codibly.cleanchargebackend.service.OptimalChargingWindowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargingWindowController.class)
@AutoConfigureMockMvc(addFilters = false) // <-- KLUCZ: wyłącza Spring Security w testach MVC
class ChargingWindowControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OptimalChargingWindowService service;

    @Test
    @DisplayName("GET /api/optimal-charging-window: uses default region=NATIONAL when region param not provided")
    void getOptimalWindow_usesDefaultRegion() throws Exception {
        // given
        OffsetDateTime start = OffsetDateTime.parse("2025-01-01T10:00:00Z");
        OffsetDateTime end   = OffsetDateTime.parse("2025-01-01T12:00:00Z");

        OptimalChargingWindow window = new OptimalChargingWindow(
                RegionScope.NATIONAL, start, end, 55.5
        );

        when(service.findOptimalWindow(eq(2), eq(RegionScope.NATIONAL))).thenReturn(window);

        // when / then
        mockMvc.perform(get("/api/optimal-charging-window")
                        .param("hours", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.region").value("NATIONAL"))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(55.5));

        verify(service).findOptimalWindow(2, RegionScope.NATIONAL);
    }

    @Test
    @DisplayName("GET /api/optimal-charging-window: passes region param to service")
    void getOptimalWindow_passesRegionParam() throws Exception {
        // given
        OffsetDateTime start = OffsetDateTime.parse("2025-01-02T01:00:00Z");
        OffsetDateTime end   = OffsetDateTime.parse("2025-01-02T02:00:00Z");

        OptimalChargingWindow window = new OptimalChargingWindow(
                RegionScope.ENGLAND, start, end, 88.0
        );

        when(service.findOptimalWindow(eq(1), eq(RegionScope.ENGLAND))).thenReturn(window);

        // when
        mockMvc.perform(get("/api/optimal-charging-window")
                        .param("hours", "1")
                        .param("region", "ENGLAND"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.region").value("ENGLAND"))
                .andExpect(jsonPath("$.start").exists())
                .andExpect(jsonPath("$.end").exists())
                .andExpect(jsonPath("$.averageCleanEnergyPercentage").value(88.0));

        ArgumentCaptor<Integer> hoursCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<RegionScope> regionCap = ArgumentCaptor.forClass(RegionScope.class);

        verify(service).findOptimalWindow(hoursCap.capture(), regionCap.capture());
        assertThat(hoursCap.getValue()).isEqualTo(1);
        assertThat(regionCap.getValue()).isEqualTo(RegionScope.ENGLAND);
    }
}
