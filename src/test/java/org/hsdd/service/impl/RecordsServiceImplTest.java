package org.hsdd.service.impl;

import org.hsdd.dto.PredictionWithAnnotationDto;
import org.hsdd.dto.SymptomDto;
import org.hsdd.value.Prediction;
import org.hsdd.value.SymptomEntry;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.repo.SymptomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordsServiceImplTest {

    @Mock
    private SymptomRepository symptoms;

    @Mock
    private PredictionRepository preds;

    @InjectMocks
    private RecordsServiceImpl service;

    // ------------------------------------------------------------------------
    // 1. listSymptoms() — maps Symptom → SymptomDto including JSON tags
    // ------------------------------------------------------------------------
    @Test
    void listSymptoms_mapsEntitiesToDtosCorrectly() {

        SymptomEntry s = new SymptomEntry(
                "PAT-12345",
                "Headache",
                "[\"pressure\",\"fatigue\"]"
        );
        s.setId(10L);
        s.setSubmittedAt(LocalDateTime.parse("2025-01-01T12:00:00"));

        when(symptoms.findTop200ByPatientIdOrderBySubmittedAtDesc("PAT-12345"))
                .thenReturn(List.of(s));

        List<SymptomDto> result = service.listSymptoms("PAT-12345");

        assertEquals(1, result.size());
        SymptomDto dto = result.get(0);

        assertEquals(10L, dto.id());
        assertEquals("Headache", dto.text());
        assertEquals(List.of("pressure", "fatigue"), dto.tags());
        assertEquals(LocalDateTime.parse("2025-01-01T12:00:00"), dto.submittedAt());

        verify(symptoms).findTop200ByPatientIdOrderBySubmittedAtDesc("PAT-12345");
    }

    // ------------------------------------------------------------------------
    // 2. listPredictions() — maps Prediction → PredictionDto correctly
    // ------------------------------------------------------------------------
    @Test
    void listPredictions_mapsEntitiesToDtosCorrectly() {

        Prediction p = new Prediction(
                "PAT-12345",
                55L,
                "HSDD",
                0.62
        );
        p.setId(200L);
        p.setCreatedAt(LocalDateTime.parse("2025-01-02T08:30:00"));

        when(preds.findTop200ByPatientIdOrderByCreatedAtDesc("PAT-12345"))
                .thenReturn(List.of(p));

        List<PredictionWithAnnotationDto> result = service.listPredictions("PAT-12345");

        assertEquals(1, result.size());
        PredictionWithAnnotationDto dto = result.get(0);

        assertEquals(200L, dto.id());
        assertEquals(55L, dto.symptomId());
        assertEquals("HSDD", dto.label());
        assertEquals(0.62, dto.confidence());
        assertEquals(LocalDateTime.parse("2025-01-02T08:30:00"), dto.createdAt());

        verify(preds).findTop200ByPatientIdOrderByCreatedAtDesc("PAT-12345");
    }

    // ------------------------------------------------------------------------
    // 3. listSymptoms() — empty list returns empty DTO list
    // ------------------------------------------------------------------------
    @Test
    void listSymptoms_emptyListReturnsEmpty() {

        when(symptoms.findTop200ByPatientIdOrderBySubmittedAtDesc("EMPTY"))
                .thenReturn(List.of());

        List<SymptomDto> result = service.listSymptoms("EMPTY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(symptoms).findTop200ByPatientIdOrderBySubmittedAtDesc("EMPTY");
    }
}
