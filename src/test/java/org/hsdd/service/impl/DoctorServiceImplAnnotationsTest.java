package org.hsdd.service.impl;

import org.hsdd.model.User;
import org.hsdd.dto.AnnotationDto;
import org.hsdd.dto.CreateAnnotationRequest;
import org.hsdd.dto.UpdateAnnotationRequest;
import org.hsdd.value.Annotation;
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
class DoctorServiceImplAnnotationsTest {

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
        doctor.setUsername("dr_jess");

        prediction = new Prediction();
        prediction.setId(100L);
        prediction.setPatientId("PAT-1");
        prediction.setSymptomId(10L);
        prediction.setLabel("HSDD");
        prediction.setConfidence(0.8);
        prediction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAnnotationsForPrediction_returnsMappedDtos() {
        Annotation a = new Annotation();
        a.setDoctor(doctor);
        a.setPrediction(prediction);
        a.setNotes("Some notes");
        a.setCorrectedLabel("Corrected");
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now();
        // using reflection-ish: fields already default to now, but it's fine
        // we just rely that dto copies whatever is in entity

        when(annotations.findByPredictionId(100L))
                .thenReturn(List.of(a));

        List<AnnotationDto> result = service.getAnnotationsForPrediction(100L);

        assertEquals(1, result.size());
        AnnotationDto dto = result.get(0);

        assertEquals(prediction.getId(), dto.predictionId());
        assertEquals(doctor.getUsername(), dto.doctorUsername());
        assertEquals(a.getNotes(), dto.notes());
        assertEquals(a.getCorrectedLabel(), dto.correctedLabel());
        assertNotNull(dto.createdAt());
        assertNotNull(dto.updatedAt());
    }

    @Test
    void createAnnotation_happyPath_savesAndLogs() {
        when(users.findByUsername("dr_jess"))
                .thenReturn(Optional.of(doctor));
        when(predictions.findById(100L))
                .thenReturn(Optional.of(prediction));

        when(annotations.save(any(Annotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateAnnotationRequest req = new CreateAnnotationRequest(
                100L,
                "Please follow up",
                "HSDD"
        );

        AnnotationDto dto = service.createAnnotation(req, "dr_jess");

        assertEquals("dr_jess", dto.doctorUsername());
        assertEquals(100L, dto.predictionId());
        assertEquals("Please follow up", dto.notes());
        assertEquals("HSDD", dto.correctedLabel());

        ArgumentCaptor<Annotation> captor = ArgumentCaptor.forClass(Annotation.class);
        verify(annotations).save(captor.capture());
        Annotation saved = captor.getValue();

        assertEquals(doctor, saved.getDoctor());
        assertEquals(prediction, saved.getPrediction());
        assertEquals("Please follow up", saved.getNotes());
        assertEquals("HSDD", saved.getCorrectedLabel());

        verify(audit).log("dr_jess",
                "DOCTOR_CREATE_ANNOTATION",
                "predictionId=100");
    }

    @Test
    void createAnnotation_whenDoctorNotFound_throws() {
        when(users.findByUsername("missing"))
                .thenReturn(Optional.empty());

        CreateAnnotationRequest req = new CreateAnnotationRequest(
                100L,
                "notes",
                "label"
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createAnnotation(req, "missing"));

        assertEquals("Doctor not found", ex.getMessage());
        verifyNoInteractions(predictions, annotations, audit);
    }

    @Test
    void updateAnnotation_happyPath_updatesAndLogs() {
        Annotation existing = new Annotation();
        existing.setDoctor(doctor);
        existing.setPrediction(prediction);
        existing.setNotes("Old");
        existing.setCorrectedLabel("OldLabel");

        when(annotations.findById(5L))
                .thenReturn(Optional.of(existing));
        when(annotations.save(any(Annotation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateAnnotationRequest req = new UpdateAnnotationRequest(
                "New notes",
                "NewLabel"
        );

        AnnotationDto dto = service.updateAnnotation(5L, req, "dr_jess");

        assertEquals("New notes", dto.notes());
        assertEquals("NewLabel", dto.correctedLabel());

        assertEquals("New notes", existing.getNotes());
        assertEquals("NewLabel", existing.getCorrectedLabel());

        verify(audit).log("dr_jess",
                "DOCTOR_UPDATE_ANNOTATION",
                "annotationId=5");
    }

    @Test
    void updateAnnotation_wrongDoctor_throws() {
        User other = new User();
        other.setUsername("otherDoc");

        Annotation existing = new Annotation();
        existing.setDoctor(other);
        existing.setPrediction(prediction);

        when(annotations.findById(5L))
                .thenReturn(Optional.of(existing));

        UpdateAnnotationRequest req = new UpdateAnnotationRequest(
                "New notes",
                "NewLabel"
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateAnnotation(5L, req, "dr_jess"));

        assertTrue(ex.getMessage().contains("Unauthorized"));
        verify(annotations, never()).save(any());
        verifyNoInteractions(audit);
    }
}
