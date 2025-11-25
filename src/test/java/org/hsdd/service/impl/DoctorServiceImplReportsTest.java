package org.hsdd.service.impl;

import org.hsdd.domain.Patient;
import org.hsdd.dto.AllReportDto;
import org.hsdd.model.Prediction;
import org.hsdd.repo.*;
import org.hsdd.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplReportsTest {

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
    void getAllReports_mapsPredictionsToAllReportDto() {
        LocalDateTime t1 = LocalDateTime.now().minusDays(1);
        LocalDateTime t2 = LocalDateTime.now();

        Prediction p1 = new Prediction();
        p1.setId(1L);
        p1.setPatientId("PAT-1");
        p1.setSymptomId(10L);
        p1.setLabel("HSDD");
        p1.setConfidence(0.9);
        p1.setCreatedAt(t1);

        Prediction p2 = new Prediction();
        p2.setId(2L);
        p2.setPatientId("PAT-2");
        p2.setSymptomId(11L);
        p2.setLabel("Anxiety");
        p2.setConfidence(0.8);
        p2.setCreatedAt(t2);

        Patient patient1 = new Patient();
        patient1.setPatientId("PAT-1");
        patient1.setFirstName("Alice");
        patient1.setLastName("Smith");

        when(predictions.findAll())
                .thenReturn(List.of(p1, p2));
        when(patients.findByPatientId("PAT-1"))
                .thenReturn(Optional.of(patient1));
        when(patients.findByPatientId("PAT-2"))
                .thenReturn(Optional.empty());

        List<AllReportDto> result = service.getAllReports();

        assertEquals(2, result.size());

        AllReportDto r1 = result.get(0);
        AllReportDto r2 = result.get(1);

        assertEquals(1L, r1.id());
        assertEquals("Alice Smith", r1.patientName());
        assertEquals("PAT-1", r1.patientId());
        assertEquals("HSDD", r1.disease());
        assertEquals(0.9, r1.confidence());
        assertEquals(
                t1.atZone(ZoneId.systemDefault()).toInstant(),
                r1.createdAt()
        );
        assertEquals("Dr. Carter", r1.doctor());

        assertEquals(2L, r2.id());
        assertEquals("Unknown", r2.patientName());
        assertEquals("PAT-2", r2.patientId());
        assertEquals("Anxiety", r2.disease());
        assertEquals(0.8, r2.confidence());
        assertEquals(
                t2.atZone(ZoneId.systemDefault()).toInstant(),
                r2.createdAt()
        );
        assertEquals("Dr. Carter", r2.doctor());

    }
}
