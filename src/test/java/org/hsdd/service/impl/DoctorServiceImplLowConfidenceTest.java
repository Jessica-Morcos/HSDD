package org.hsdd.service.impl;

import org.hsdd.dto.LowConfidenceDto;
import org.hsdd.repo.AnnotationRepository;
import org.hsdd.repo.IssueReportRepository;
import org.hsdd.repo.PatientRepository;
import org.hsdd.repo.PredictionRepository;
import org.hsdd.repo.SymptomRepository;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.AuditService;
import org.hsdd.value.Prediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplLowConfidenceTest {

    @Mock
    AnnotationRepository annotations;

    @Mock
    IssueReportRepository issues;

    @Mock
    PredictionRepository predictions;

    @Mock
    UserRepository users;

    @Mock
    PatientRepository patients;

    @Mock
    SymptomRepository symptoms;

    @Mock
    AuditService audit;

    private DoctorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorServiceImpl(
                annotations,
                issues,
                predictions,
                users,
                patients,
                symptoms,
                audit
        );
    }

    @Test
    void getAllLowConfidenceReports_usesSimpleThresholdFilter() {
        LocalDateTime now = LocalDateTime.now();

        // prediction below threshold, already reviewed
        Prediction p = new Prediction();
        p.setId(2L);
        p.setPatientId("PAT-2");
        p.setSymptomId(11L);
        p.setLabel("Anxiety");
        p.setConfidence(0.30);
        p.setCreatedAt(now);
        p.setReviewed(true);

        when(predictions.findByConfidenceLessThan(0.55))
                .thenReturn(List.of(p));

        // unknown patient => "Unknown"
        when(patients.findByPatientId("PAT-2"))
                .thenReturn(Optional.empty());

        // no symptom description
        when(symptoms.findById(11L))
                .thenReturn(Optional.empty());

        List<LowConfidenceDto> result = service.getAllLowConfidenceReports();

        assertEquals(1, result.size());
        LowConfidenceDto dto = result.get(0);

        assertEquals(2L, dto.id());
        assertEquals("Unknown", dto.patientName());
        assertEquals("PAT-2", dto.patientId());
        assertEquals("Anxiety", dto.predictedDisease());
        assertEquals(0.30, dto.confidence());
        // ✅ because p.setReviewed(true)
        assertEquals("Reviewed", dto.status());
    }

    @Test
    void markLowConfidenceReviewed_setsReviewedTrueAndLogs() {
        Prediction p = new Prediction();
        p.setId(10L);
        p.setPatientId("PAT-10");
        p.setSymptomId(100L);
        p.setLabel("Label");
        p.setConfidence(0.40);
        p.setCreatedAt(LocalDateTime.now());
        p.setReviewed(false);

        when(predictions.findById(10L))
                .thenReturn(Optional.of(p));

        service.markLowConfidenceReviewed(10L, "doctorUser");

        assertTrue(p.isReviewed());
        verify(predictions).save(p);
        verify(audit).log(
                "doctorUser",
                "DOCTOR_REVIEW_LOW_CONFIDENCE",
                "predictionId=10"
        );
    }

    @Test
    void markLowConfidencePending_setsReviewedFalse() {
        Prediction p = new Prediction();
        p.setId(5L);
        p.setPatientId("PAT-5");
        p.setReviewed(true);
        p.setCreatedAt(LocalDateTime.now());

        when(predictions.findById(5L))
                .thenReturn(Optional.of(p));

        service.markLowConfidencePending(5L);

        assertFalse(p.isReviewed());
        verify(predictions).save(p);
        // no audit log expected here
        verifyNoInteractions(audit);
    }
}
