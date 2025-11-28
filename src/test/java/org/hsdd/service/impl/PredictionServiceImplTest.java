package org.hsdd.service.impl;

import org.hsdd.value.Prediction;
import org.hsdd.value.SymptomEntry;
import org.hsdd.model.AI;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.service.NotificationService;
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

    @Mock
    private AI predictor;

    @InjectMocks
    private PredictionServiceImpl service;

    // -------------------------------------------------------------------------
    // 1. inferAndSave() — saves prediction with correct values
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_savesPredictionWithCorrectData() {

        SymptomEntry symptom = new SymptomEntry("PAT-12345", "Cough", "[\"tag1\"]");
        symptom.setId(10L);

        // predictor must return a new Prediction
        Prediction generated = new Prediction("PAT-12345", 10L, "Flu", 0.30);

        when(predictor.predict(symptom)).thenReturn(generated);

        // repo save returns a saved prediction
        Prediction saved = new Prediction("PAT-12345", 10L, "Flu", 0.30);
        saved.setId(99L);

        when(predictions.save(generated)).thenReturn(saved);

        Prediction result = service.inferAndSave(symptom, "doctorA", "127.0.0.1");

        assertNotNull(result);
        assertEquals("PAT-12345", result.getPatientId());
        assertEquals(10L, result.getSymptomId());
        assertEquals("Flu", result.getLabel());
        assertEquals(0.30, result.getConfidence());
        assertEquals(99L, result.getId());
    }

    // -------------------------------------------------------------------------
    // 2. inferAndSave() — triggers notifyLowConfidence()
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_triggersLowConfidenceNotification() {

        SymptomEntry symptom = new SymptomEntry("PAT-99999", "Headache", null);
        symptom.setId(20L);

        Prediction generated = new Prediction("PAT-99999", 20L, "Flu", 0.30);
        when(predictor.predict(symptom)).thenReturn(generated);

        Prediction saved = new Prediction("PAT-99999", 20L, "Flu", 0.30);
        saved.setId(200L);

        when(predictions.save(generated)).thenReturn(saved);

        service.inferAndSave(symptom, "actor", "10.0.0.1");

        verify(notificationService, times(1))
                .notifyLowConfidence(saved, 0.50);
    }

    // -------------------------------------------------------------------------
    // 3. inferAndSave() — verify prediction passed to repo.save()
    // -------------------------------------------------------------------------
    @Test
    void inferAndSave_passesCorrectPredictionToRepository() {

        SymptomEntry symptom = new SymptomEntry("PAT-77777", "Chest pain", "[]");
        symptom.setId(55L);

        Prediction generated = new Prediction("PAT-77777", 55L, "Flu", 0.30);
        when(predictor.predict(symptom)).thenReturn(generated);

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
