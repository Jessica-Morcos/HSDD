package org.hsdd.controller;

import org.hsdd.model.MedicalHistory;
import org.hsdd.security.TokenAuthFilter;
import org.hsdd.service.MedicalHistoryService;
import org.hsdd.web.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class MedicalHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicalHistoryService historyService;

    @MockBean
    private TokenAuthFilter tokenAuthFilter;


    // ---------------------------------------------------------
    // 1) uploadHistory() tests
    // ---------------------------------------------------------

    @Test
    void uploadHistory_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "history.txt", "text/plain", "sample data".getBytes()
        );

        mockMvc.perform(multipart("/api/history/upload")
                        .file(file)
                        .param("patientId", "12345678"))
                .andExpect(status().isOk());

        verify(historyService).uploadHistoryFile(eq("12345678"), any());
    }

    @Test
    void uploadHistory_missingFile_returns400() throws Exception {
        mockMvc.perform(multipart("/api/history/upload")
                        .param("patientId", "12345678"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadHistory_serviceThrowsIOException_returns500() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.txt", "text/plain", "abc".getBytes()
        );

        doThrow(new IOException("err"))
                .when(historyService).uploadHistoryFile(eq("12345678"), any());

        mockMvc.perform(multipart("/api/history/upload")
                        .file(file)
                        .param("patientId", "12345678"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------------
    // 2) listHistory() tests
    // ---------------------------------------------------------

    @Test
    void listHistory_success() throws Exception {
        MedicalHistory e = new MedicalHistory();
        e.setId(1L);
        e.setPatientId("12345678");
        e.setTitle("Asthma");
        e.setDetails("Uses inhaler");

        when(historyService.listHistory("12345678"))
                .thenReturn(List.of(e));

        mockMvc.perform(get("/api/history")
                        .param("patientId", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Asthma"));
    }

    @Test
    void listHistory_missingPatientId_returns400() throws Exception {
        mockMvc.perform(get("/api/history"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listHistory_serviceThrowsRuntimeException_returns500() throws Exception {
        when(historyService.listHistory("bad"))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/history")
                        .param("patientId", "bad"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------------
    // 3) addHistoryEntry() tests
    // ---------------------------------------------------------

    @Test
    void addHistoryEntry_success() throws Exception {
        MedicalHistory e = new MedicalHistory();
        e.setId(10L);
        e.setPatientId("12345678");
        e.setTitle("Diabetes");
        e.setDetails("Chronic");

        when(historyService.save(any())).thenReturn(e);

        mockMvc.perform(post("/api/history/add")
                        .param("patientId", "12345678")
                        .param("title", "Diabetes")
                        .param("details", "Chronic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void addHistoryEntry_missingParams_returns400() throws Exception {
        mockMvc.perform(post("/api/history/add")
                        .param("patientId", "12345678")
                        .param("title", "Only title"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addHistoryEntry_serviceThrowsException_returns500() throws Exception {
        when(historyService.save(any()))
                .thenThrow(new RuntimeException("crash"));

        mockMvc.perform(post("/api/history/add")
                        .param("patientId", "12345678")
                        .param("title", "x")
                        .param("details", "y"))
                .andExpect(status().isInternalServerError());
    }
}
