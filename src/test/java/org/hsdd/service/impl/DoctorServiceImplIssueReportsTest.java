package org.hsdd.service.impl;

import org.hsdd.model.User;
import org.hsdd.dto.IssueReportDto;
import org.hsdd.dto.ReportIssueRequest;
import org.hsdd.model.IssueReport;
import org.hsdd.value.Prediction;
import org.hsdd.repo.*;
import org.hsdd.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplIssueReportsTest {

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

    private User doctor;
    private Prediction prediction;

    @BeforeEach
    void setUp() {
        doctor = new User();
        doctor.setId(1L);
        doctor.setUsername("doctorUser");

        prediction = new Prediction();
        prediction.setId(200L);
        prediction.setPatientId("PAT-2");
        prediction.setSymptomId(20L);
        prediction.setLabel("Anxiety");
        prediction.setConfidence(0.6);
        prediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getIssuesForPrediction_returnsDtos() {
        IssueReport r = new IssueReport();
        r.setDoctor(doctor);
        r.setPrediction(prediction);
        r.setIssueDescription("Ambiguous case");
        r.setCorrectLabel("Corrected");
        // createdAt auto

        when(issues.findByPredictionId(200L))
                .thenReturn(List.of(r));

        List<IssueReportDto> result = service.getIssuesForPrediction(200L);

        assertEquals(1, result.size());
        IssueReportDto dto = result.get(0);

        assertEquals(prediction.getId(), dto.predictionId());
        assertEquals(doctor.getUsername(), dto.doctorUsername());
        assertEquals("Ambiguous case", dto.issueDescription());
        assertEquals("Corrected", dto.correctLabel());
        assertNotNull(dto.createdAt());
    }

    @Test
    void reportIssue_happyPath_savesAndLogs() {
        when(users.findByUsername("doctorUser"))
                .thenReturn(Optional.of(doctor));
        when(predictions.findById(200L))
                .thenReturn(Optional.of(prediction));

        when(issues.save(any(IssueReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReportIssueRequest req = new ReportIssueRequest(
                200L,
                "Model seems off",
                "BetterLabel"
        );

        IssueReportDto dto = service.reportIssue(req, "doctorUser");

        assertEquals("doctorUser", dto.doctorUsername());
        assertEquals(200L, dto.predictionId());
        assertEquals("Model seems off", dto.issueDescription());
        assertEquals("BetterLabel", dto.correctLabel());

        ArgumentCaptor<IssueReport> captor = ArgumentCaptor.forClass(IssueReport.class);
        verify(issues).save(captor.capture());
        IssueReport saved = captor.getValue();

        assertEquals(doctor, saved.getDoctor());
        assertEquals(prediction, saved.getPrediction());
        assertEquals("Model seems off", saved.getIssueDescription());
        assertEquals("BetterLabel", saved.getCorrectLabel());

        verify(audit).log("doctorUser",
                "DOCTOR_REPORT_ISSUE",
                "predictionId=200");
    }

    @Test
    void reportIssue_doctorNotFound_throws() {
        when(users.findByUsername("missing"))
                .thenReturn(Optional.empty());

        ReportIssueRequest req = new ReportIssueRequest(
                200L,
                "Issue",
                "Label"
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.reportIssue(req, "missing"));

        assertEquals("Doctor not found", ex.getMessage());
        verifyNoInteractions(predictions, issues, audit);
    }
}
