package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.repo.UserRepository;
import org.hsdd.security.TokenAuthFilter;
import org.hsdd.service.SymptomService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SymptomController.class)
@AutoConfigureMockMvc(addFilters = false)
class SymptomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock required by security filter chain
    @MockBean private TokenAuthFilter tokenAuthFilter;
    @MockBean private UserRepository userRepository;

    @MockBean
    private SymptomService symptomService;

    // -----------------------------------------------------------------------------
    // 1. SUCCESSFUL SUBMISSION — validates JSON response structure
    // -----------------------------------------------------------------------------
    @Test
    void submitSymptom_returnsSymptomAndPrediction() throws Exception {

        SymptomDto symptom = new SymptomDto(
                10L,
                "Stomach pain",
                List.of("pain", "abdomen"),
                LocalDateTime.now()
        );

        PredictionDto prediction = new PredictionDto(
                99L,
                10L,
                "HSDD",
                0.91,
                "HIGH",        // <-- NEW FIELD REQUIRED
                LocalDateTime.now()
        );


        SubmitSymptomResponse response = new SubmitSymptomResponse(symptom, prediction);

        when(symptomService.submit(any(), any(), any()))
                .thenReturn(response);

        String json = """
        {
          "patientId": "PAT-1",
          "text": "Stomach pain",
          "tags": ["pain","abdomen"]
        }
        """;

        mockMvc.perform(post("/api/patient/symptoms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptom.id").value(10))
                .andExpect(jsonPath("$.symptom.text").value("Stomach pain"))
                .andExpect(jsonPath("$.prediction.label").value("HSDD"))
                .andExpect(jsonPath("$.prediction.confidence").value(0.91));
    }

    // -----------------------------------------------------------------------------
    // 2. ACTOR EXTRACTION — checks that principal username is passed to service
    // -----------------------------------------------------------------------------
    @Test
    void submitSymptom_usesPrincipalAsActor() throws Exception {

        when(symptomService.submit(any(), any(), any()))
                .thenReturn(new SubmitSymptomResponse(null, null));

        String json = """
        {
          "patientId": "PAT-2",
          "text": "Headache",
          "tags": []
        }
        """;

        mockMvc.perform(post("/api/patient/symptoms")
                        .principal(() -> "jessicaUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<String> actorCaptor = ArgumentCaptor.forClass(String.class);
        verify(symptomService).submit(any(), actorCaptor.capture(), any());

        assert(actorCaptor.getValue().equals("jessicaUser"));
    }

    // -----------------------------------------------------------------------------
    // 3. IP EXTRACTION — ensures remote IP is passed correctly to service
    // -----------------------------------------------------------------------------
    @Test
    void submitSymptom_passesClientIpToService() throws Exception {

        when(symptomService.submit(any(), any(), any()))
                .thenReturn(new SubmitSymptomResponse(null, null));

        String json = """
        {
          "patientId": "PAT-99",
          "text": "Chest tightness",
          "tags": ["respiratory"]
        }
        """;

        mockMvc.perform(post("/api/patient/symptoms")
                        .with(req -> { req.setRemoteAddr("123.45.67.89"); return req; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(symptomService).submit(any(), any(), ipCaptor.capture());

        assert(ipCaptor.getValue().equals("123.45.67.89"));
    }
}
