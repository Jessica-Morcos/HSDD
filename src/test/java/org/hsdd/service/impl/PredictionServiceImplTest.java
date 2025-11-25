package org.hsdd.service.impl;

import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.service.NotificationService;
import org.hsdd.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private PredictionRepository predictions;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PredictionServiceImpl service;

    // -------------------------------------------------------------------------
    // 1. inferAndSave() — saves prediction with correct values
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_savesPredictionWithCorrectData() {

        Symptom symptom = new Symptom("PAT-12345", "Cough", "[\"tag1\"]");
        symptom.setId(10L);

        // Mock repo save → return saved prediction object
        Prediction saved = new Prediction("PAT-12345", 10L, "Flu", 0.30);
        saved.setId(99L);

        when(predictions.save(any(Prediction.class))).thenReturn(saved);

        Prediction result = service.inferAndSave(symptom, "doctorA", "127.0.0.1");

        assertNotNull(result);
        assertEquals("PAT-12345", result.getPatientId());
        assertEquals(10L, result.getSymptomId());
        assertEquals("Flu", result.getLabel());
        assertEquals(0.30, result.getConfidence());
        assertEquals(99L, result.getId());
    }

    // -------------------------------------------------------------------------
    // 2. inferAndSave() — triggers notifyLowConfidence() ALWAYS (0.30 threshold)
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_triggersLowConfidenceNotification() {

        Symptom symptom = new Symptom("PAT-99999", "Headache", null);
        symptom.setId(20L);

        Prediction saved = new Prediction("PAT-99999", 20L, "Flu", 0.30);
        saved.setId(200L);

        when(predictions.save(any(Prediction.class))).thenReturn(saved);

        service.inferAndSave(symptom, "actor", "10.0.0.1");

        verify(notificationService, times(1))
                .notifyLowConfidence(saved, 0.50);
    }

    // -------------------------------------------------------------------------
    // 3. inferAndSave() — prediction passed to repo.save() has correct fields
    //    (verify the object BEFORE it is returned)
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_passesCorrectPredictionToRepository() {

        Symptom symptom = new Symptom("PAT-77777", "Chest pain", "[]");
        symptom.setId(55L);

        when(predictions.save(any(Prediction.class)))
                .thenAnswer(inv -> {
                    Prediction p = inv.getArgument(0);
                    p.setId(500L);
                    return p;
                });

        service.inferAndSave(symptom, "actorX", "8.8.8.8");

        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(predictions).save(captor.capture());

        Prediction captured = captor.getValue();

        assertEquals("PAT-77777", captured.getPatientId());
        assertEquals(55L, captured.getSymptomId());
        assertEquals("Flu", captured.getLabel());
        assertEquals(0.30, captured.getConfidence());
        assertFalse(captured.isReviewed());
    }
}
