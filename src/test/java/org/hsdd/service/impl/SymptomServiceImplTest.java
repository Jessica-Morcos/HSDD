package org.hsdd.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.hsdd.domain.Patient;
import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.SubmitSymptomRequest;
import org.hsdd.dto.SubmitSymptomResponse;
import org.hsdd.dto.SymptomDto;
import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.SymptomRepository;
import org.hsdd.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomServiceImplTest {

    @Mock
    private SymptomRepository symptoms;

    @Mock
    private PredictionService predictions;

    @Mock
    private PatientRepository patients;

    @InjectMocks
    private SymptomServiceImpl service;

    // ------------------------------------------------------------------------------------
    // 1. SUCCESSFUL SUBMIT — saves symptom + calls prediction + returns DTOs
    // ------------------------------------------------------------------------------------
    @Test
    void submit_successfulFlow_returnsSubmitSymptomResponse() {

        SubmitSymptomRequest req = new SubmitSymptomRequest(
                "PAT-12345",
                "Severe headaches",
                List.of("migraine", "vision")
        );

        Patient patient = new Patient();
        patient.setPatientId("PAT-12345");

        when(patients.findByPatientId("PAT-12345"))
                .thenReturn(Optional.of(patient));

        Symptom savedSymptom = new Symptom("PAT-12345", "Severe headaches", "[\"migraine\",\"vision\"]");
        savedSymptom.setId(10L);
        savedSymptom.setSubmittedAt(LocalDateTime.parse("2025-01-01T10:00:00"));

        when(symptoms.save(any())).thenReturn(savedSymptom);

        Prediction prediction = new Prediction("PAT-12345", 10L, "HSDD", 0.77);
        prediction.setId(50L);
        prediction.setCreatedAt(LocalDateTime.parse("2025-01-01T10:05:00"));

        when(predictions.inferAndSave(any(), eq("actorX"), eq("5.5.5.5")))
                .thenReturn(prediction);

        SubmitSymptomResponse res = service.submit(req, "actorX", "5.5.5.5");

        // Assert SymptomDto
        assertEquals(10L, res.symptom().id());
        assertEquals("Severe headaches", res.symptom().text());
        assertEquals(List.of("migraine", "vision"), res.symptom().tags());
        assertEquals(LocalDateTime.parse("2025-01-01T10:00:00"), res.symptom().submittedAt());

        // Assert PredictionDto
        assertEquals(50L, res.prediction().id());
        assertEquals(10L, res.prediction().symptomId());
        assertEquals("HSDD", res.prediction().label());
        assertEquals(0.77, res.prediction().confidence());
        assertEquals(LocalDateTime.parse("2025-01-01T10:05:00"), res.prediction().createdAt());

        // Verify save
        verify(symptoms, times(1)).save(any(Symptom.class));
        verify(predictions).inferAndSave(any(Symptom.class), eq("actorX"), eq("5.5.5.5"));
    }


    // ------------------------------------------------------------------------------------
    // 2. TAG PARSING — ensure JSON array tags are correctly mapped back to Java List
    // ------------------------------------------------------------------------------------
    @Test
    void submit_correctlyParsesTagsJson() {

        SubmitSymptomRequest req = new SubmitSymptomRequest(
                "PAT-99999",
                "Fatigue",
                List.of("tired", "low_energy")
        );

        Patient patient = new Patient();
        patient.setPatientId("PAT-99999");

        when(patients.findByPatientId("PAT-99999"))
                .thenReturn(Optional.of(patient));

        Symptom saved = new Symptom("PAT-99999", "Fatigue", "[\"tired\",\"low_energy\"]");
        saved.setId(20L);
        saved.setSubmittedAt(LocalDateTime.now());

        when(symptoms.save(any())).thenReturn(saved);

        Prediction pred = new Prediction("PAT-99999", 20L, "HSDD", 0.45);
        pred.setId(60L);
        pred.setCreatedAt(LocalDateTime.now());

        when(predictions.inferAndSave(any(), any(), any()))
                .thenReturn(pred);

        SubmitSymptomResponse res = service.submit(req, "actorY", "77.7.7.7");

        assertEquals(List.of("tired", "low_energy"), res.symptom().tags());
        assertEquals("Fatigue", res.symptom().text());
    }


    // ------------------------------------------------------------------------------------
    // 3. PATIENT NOT FOUND — throws RuntimeException
    // ------------------------------------------------------------------------------------
    @Test
    void submit_patientNotFound_throwsException() {

        SubmitSymptomRequest req = new SubmitSymptomRequest(
                "PAT-00000",
                "Pain",
                List.of("sharp")
        );

        when(patients.findByPatientId("PAT-00000"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.submit(req, "actor", "1.1.1.1")
        );

        verify(symptoms, never()).save(any());
        verify(predictions, never()).inferAndSave(any(), any(), any());
    }
}
