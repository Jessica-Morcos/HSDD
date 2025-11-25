package org.hsdd.controller;

import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.SymptomDto;
import org.hsdd.repo.UserRepository;
import org.hsdd.security.TokenAuthFilter;
import org.hsdd.service.RecordsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordsController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecordsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // --- REQUIRED BECAUSE SECURITY FILTER DEPENDS ON THEM ---
    @MockBean
    private TokenAuthFilter tokenAuthFilter;

    @MockBean
    private UserRepository userRepository;

    // --- SERVICE UNDER TEST ---
    @MockBean
    private RecordsService recordsService;

    // -------------------------------------------------------------------------
    // SYMPTOMS
    // -------------------------------------------------------------------------

    @Test
    void listSymptoms_returnsList() throws Exception {
        SymptomDto s1 = new SymptomDto(
                1L,
                "Short description",
                List.of("tag1","tag2"),
                LocalDateTime.now()
        );

        when(recordsService.listSymptoms("PAT-1"))
                .thenReturn(List.of(s1));

        mockMvc.perform(get("/api/records/symptoms")
                        .param("patientId", "PAT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Short description"))
                .andExpect(jsonPath("$[0].tags[0]").value("tag1"))
                .andExpect(jsonPath("$[0].tags[1]").value("tag2"));
    }

    @Test
    void listSymptoms_emptyListWhenNone() throws Exception {
        when(recordsService.listSymptoms("EMPTY"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/records/symptoms")
                        .param("patientId", "EMPTY"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // PREDICTIONS
    // -------------------------------------------------------------------------

    @Test
    void listPredictions_returnsList() throws Exception {
        PredictionDto p = new PredictionDto(
                5L,
                10L,
                "HSDD",
                0.83,
                LocalDateTime.now()
        );

        when(recordsService.listPredictions("PAT-5"))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/records/predictions")
                        .param("patientId", "PAT-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].symptomId").value(10))
                .andExpect(jsonPath("$[0].label").value("HSDD"))
                .andExpect(jsonPath("$[0].confidence").value(0.83));
    }

    @Test
    void listPredictions_emptyListWhenNone() throws Exception {
        when(recordsService.listPredictions("EMPTY"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/records/predictions")
                        .param("patientId", "EMPTY"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
