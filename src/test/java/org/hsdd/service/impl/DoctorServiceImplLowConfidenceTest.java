package org.hsdd.service.impl;

import org.hsdd.domain.Patient;
import org.hsdd.dto.LowConfidenceDto;
import org.hsdd.model.Prediction;
import org.hsdd.repo.*;
import org.hsdd.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplLowConfidenceTest {

    @Mock
    private AnnotationRepository annotations;
    @Mock
    private IssueReportRepository issues;
    @Mock
    private PredictionRepository predictions;
    @Mock
    private UserRepository users;
    @Mock
    private PatientRepository patients;
    @Mock
    private SymptomRepository symptoms;
    @Mock
    private AuditService audit;

    @InjectMocks
    private DoctorServiceImpl service;

    @Test
    void getLowConfidenceReports_usesReviewedFalseFilter() {
        LocalDateTime now = LocalDateTime.now();

        Prediction p = new Prediction();
        p.setId(1L);
        p.setPatientId("PAT-1");
        p.setSymptomId(10L);
        p.setLabel("HSDD");
        p.setConfidence(0.4);
        p.setCreatedAt(now);

        Patient patient = new Patient();
        patient.setPatientId("PAT-1");
        patient.setFirstName("Alice");
        patient.setLastName("Smith");
        patient.setDateOfBirth(LocalDate.now().minusYears(30));

        when(predictions.findByConfidenceLessThanAndReviewedFalse(0.55))
                .thenReturn(List.of(p));
        when(patients.findByPatientId("PAT-1"))
                .thenReturn(Optional.of(patient));

        List<LowConfidenceDto> result = service.getLowConfidenceReports();

        assertEquals(1, result.size());
        LowConfidenceDto dto = result.get(0);

        assertEquals(1L, dto.id());
        assertEquals("Alice Smith", dto.patientName());
        assertEquals("PAT-1", dto.patientId());
        assertEquals("HSDD", dto.predictedDisease());
        assertEquals(0.4, dto.confidence());
        assertEquals("Pending Review", dto.status());
    }

    @Test
    void getAllLowConfidenceReports_usesSimpleThresholdFilter() {
        LocalDateTime now = LocalDateTime.now();

        Prediction p = new Prediction();
        p.setId(2L);
        p.setPatientId("PAT-2");
        p.setSymptomId(11L);
        p.setLabel("Anxiety");
        p.setConfidence(0.3);
        p.setCreatedAt(now);
        p.setReviewed(true);

        when(predictions.findByConfidenceLessThan(0.55))
                .thenReturn(List.of(p));
        when(patients.findByPatientId("PAT-2"))
                .thenReturn(Optional.empty()); // unknown patient

        List<LowConfidenceDto> result = service.getAllLowConfidenceReports();

        assertEquals(1, result.size());
        LowConfidenceDto dto = result.get(0);

        assertEquals("Unknown", dto.patientName());
        assertEquals("PAT-2", dto.patientId());
        assertEquals("Anxiety", dto.predictedDisease());
        assertEquals("Reviewed", dto.status());
    }

    @Test
    void getLowConfidenceReport_byId_mapsFields() {
        LocalDateTime now = LocalDateTime.now();

        Prediction p = new Prediction();
        p.setId(5L);
        p.setPatientId("PAT-5");
        p.setSymptomId(50L);
        p.setLabel("HSDD");
        p.setConfidence(0.48);
        p.setCreatedAt(now);
        p.setReviewed(false);

        Patient patient = new Patient();
        patient.setPatientId("PAT-5");
        patient.setFirstName("Bob");
        patient.setLastName("Jones");

        when(predictions.findById(5L))
                .thenReturn(Optional.of(p));
        when(patients.findByPatientId("PAT-5"))
                .thenReturn(Optional.of(patient));

        LowConfidenceDto dto = service.getLowConfidenceReport(5L);

        assertEquals(5L, dto.id());
        assertEquals("Bob Jones", dto.patientName());
        assertEquals("PAT-5", dto.patientId());
        assertEquals("HSDD", dto.predictedDisease());
        assertEquals(0.48, dto.confidence());
        assertEquals("Pending Review", dto.status());
    }

    @Test
    void markLowConfidenceReviewed_setsReviewedTrueAndLogs() {
        Prediction p = new Prediction();
        p.setId(10L);
        p.setPatientId("PAT-10");
        p.setSymptomId(100L);
        p.setLabel("Label");
        p.setConfidence(0.4);
        p.setCreatedAt(LocalDateTime.now());
        p.setReviewed(false);

        when(predictions.findById(10L))
                .thenReturn(Optional.of(p));

        service.markLowConfidenceReviewed(10L, "doctorUser");

        assertTrue(p.isReviewed());
        verify(predictions).save(p);
        verify(audit).log("doctorUser",
                "DOCTOR_REVIEW_LOW_CONFIDENCE",
                "predictionId=10");
    }

    @Test
    void markLowConfidencePending_setsReviewedFalse() {
        Prediction p = new Prediction();
        p.setId(33L);
        p.setPatientId("PAT-33");
        p.setSymptomId(333L);
        p.setLabel("Label");
        p.setConfidence(0.5);
        p.setCreatedAt(LocalDateTime.now());
        p.setReviewed(true);

        when(predictions.findById(33L))
                .thenReturn(Optional.of(p));

        service.markLowConfidencePending(33L);

        assertFalse(p.isReviewed());
        verify(predictions).save(p);
        verifyNoInteractions(audit);
    }
}
