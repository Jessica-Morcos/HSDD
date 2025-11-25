package org.hsdd.service.impl;

import org.hsdd.domain.Patient;
import org.hsdd.domain.User;
import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.RecentPatientDto;
import org.hsdd.dto.TrendDataDto;
import org.hsdd.model.Annotation;
import org.hsdd.model.Prediction;
import org.hsdd.model.Symptom;
import org.hsdd.repo.*;
import org.hsdd.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplPatientsTest {

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
    void getPatientPredictions_returnsMappedDtos() {
        LocalDateTime now = LocalDateTime.now();

        Prediction p1 = new Prediction();
        p1.setId(1L);
        p1.setPatientId("PAT-1");
        p1.setSymptomId(10L);
        p1.setLabel("HSDD");
        p1.setConfidence(0.9);
        p1.setCreatedAt(now.minusDays(1));

        Prediction p2 = new Prediction();
        p2.setId(2L);
        p2.setPatientId("PAT-1");
        p2.setSymptomId(11L);
        p2.setLabel("Anxiety");
        p2.setConfidence(0.7);
        p2.setCreatedAt(now);

        when(predictions.findByPatientIdOrderByCreatedAtDesc("PAT-1"))
                .thenReturn(List.of(p2, p1)); // newest first

        List<PredictionDto> result = service.getPatientPredictions("PAT-1");

        assertEquals(2, result.size());
        PredictionDto first = result.get(0);

        assertEquals(2L, first.id());
        assertEquals(11L, first.symptomId());
        assertEquals("Anxiety", first.label());
        assertEquals(0.7, first.confidence());
    }

    @Test
    void getPatientTrends_groupsByLabelCounts() {
        LocalDateTime now = LocalDateTime.now();

        Prediction p1 = new Prediction();
        p1.setPatientId("PAT-1");
        p1.setSymptomId(1L);
        p1.setLabel("HSDD");
        p1.setConfidence(0.8);
        p1.setCreatedAt(now);

        Prediction p2 = new Prediction();
        p2.setPatientId("PAT-1");
        p2.setSymptomId(2L);
        p2.setLabel("HSDD");
        p2.setConfidence(0.9);
        p2.setCreatedAt(now.minusDays(1));

        Prediction p3 = new Prediction();
        p3.setPatientId("PAT-1");
        p3.setSymptomId(3L);
        p3.setLabel("Anxiety");
        p3.setConfidence(0.6);
        p3.setCreatedAt(now.minusDays(2));

        when(predictions.findByPatientIdOrderByCreatedAtDesc("PAT-1"))
                .thenReturn(List.of(p1, p2, p3));

        List<TrendDataDto> trends = service.getPatientTrends("PAT-1");

        assertEquals(2, trends.size());

        TrendDataDto hsd = trends.stream()
                .filter(t -> t.label().equals("HSDD"))
                .findFirst()
                .orElseThrow();
        TrendDataDto anx = trends.stream()
                .filter(t -> t.label().equals("Anxiety"))
                .findFirst()
                .orElseThrow();

        assertEquals(2L, hsd.count());
        assertEquals(1L, anx.count());
    }

    @Test
    void getRecentPatients_buildsSortedListWithDoctorNotes() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Patients
        Patient p1 = new Patient();
        p1.setPatientId("P1");
        p1.setFirstName("Alice");
        p1.setLastName("Smith");
        p1.setDateOfBirth(today.minusYears(30));

        Patient p2 = new Patient();
        p2.setPatientId("P2");
        p2.setFirstName("Bob");
        p2.setLastName("Jones");
        p2.setDateOfBirth(today.minusYears(40));

        when(patients.findAll()).thenReturn(List.of(p1, p2));

        // Symptoms
        Symptom s1 = new Symptom();
        s1.setPatientId("P1");
        s1.setSubmittedAt(now.minusDays(1));

        when(symptoms.findTop1ByPatientIdOrderBySubmittedAtDesc("P1"))
                .thenReturn(Optional.of(s1));
        when(symptoms.findTop1ByPatientIdOrderBySubmittedAtDesc("P2"))
                .thenReturn(Optional.empty());

        // Predictions (last for each)
        Prediction pred1 = new Prediction();
        pred1.setId(10L);
        pred1.setPatientId("P1");
        pred1.setSymptomId(1L);
        pred1.setLabel("Diagnosis1");
        pred1.setConfidence(0.8);
        pred1.setCreatedAt(now.minusDays(2));

        Prediction pred2 = new Prediction();
        pred2.setId(20L);
        pred2.setPatientId("P2");
        pred2.setSymptomId(2L);
        pred2.setLabel("Diagnosis2");
        pred2.setConfidence(0.7);
        pred2.setCreatedAt(now.minusDays(3));

        when(predictions.findTop1ByPatientIdOrderByCreatedAtDesc("P1"))
                .thenReturn(Optional.of(pred1));
        when(predictions.findTop1ByPatientIdOrderByCreatedAtDesc("P2"))
                .thenReturn(Optional.of(pred2));

        // Latest annotation for P1
        Annotation ann = new Annotation();
        ann.setNotes("Follow up soon");
        when(annotations.findTop1ByPredictionIdOrderByCreatedAtDesc(10L))
                .thenReturn(Optional.of(ann));
        when(annotations.findTop1ByPredictionIdOrderByCreatedAtDesc(20L))
                .thenReturn(Optional.empty());

        List<RecentPatientDto> result = service.getRecentPatients();

        assertEquals(2, result.size());

        // P1 should come first (more recent lastVisit)
        RecentPatientDto first = result.get(0);
        RecentPatientDto second = result.get(1);

        assertEquals("P1", first.patientId());
        assertEquals("Alice Smith", first.name());
        assertEquals(30, first.age());
        assertEquals("Diagnosis1", first.lastDiagnosis());
        assertEquals("Follow up soon", first.doctorNotes());

        assertEquals("P2", second.patientId());
        assertEquals("Bob Jones", second.name());
        assertEquals(40, second.age());
        assertEquals("Diagnosis2", second.lastDiagnosis());
        assertEquals("—", second.doctorNotes());
    }

    @Test
    void getAllPatients_returnsListWithoutSortingLogic() {
        // This just reuses same mapping logic as getRecentPatients, but without sort.
        LocalDate today = LocalDate.now();

        Patient p = new Patient();
        p.setPatientId("PX");
        p.setFirstName("Test");
        p.setLastName("Patient");
        p.setDateOfBirth(today.minusYears(25));

        when(patients.findAll()).thenReturn(List.of(p));
        when(symptoms.findTop1ByPatientIdOrderBySubmittedAtDesc("PX"))
                .thenReturn(Optional.empty());
        when(predictions.findTop1ByPatientIdOrderByCreatedAtDesc("PX"))
                .thenReturn(Optional.empty());

        List<RecentPatientDto> result = service.getAllPatients();

        assertEquals(1, result.size());
        RecentPatientDto dto = result.get(0);

        assertEquals("PX", dto.patientId());
        assertEquals("Test Patient", dto.name());
        assertEquals(25, dto.age());
        assertNull(dto.lastVisit());
        assertEquals("No diagnosis yet", dto.lastDiagnosis());
        assertEquals("—", dto.doctorNotes());
    }
}
