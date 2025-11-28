package org.hsdd.controller;

import static org.mockito.Mockito.*;

import org.hsdd.model.Doctor;
import org.hsdd.model.User;
import org.hsdd.dto.*;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.DoctorFactory;
import org.hsdd.service.DoctorService;
import org.hsdd.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.ServletException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        PredictionController.class,
        DoctorController.class
})
@AutoConfigureMockMvc(addFilters = false)
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserRepository users;

    @MockBean
    private DoctorFactory doctorFactory;

    @MockBean
    private Doctor mockDoctor;

    @BeforeEach
    void setupDoctorFactory() {

        when(doctorFactory.fromUsername(any()))
                .thenReturn(mockDoctor);

        when(mockDoctor.viewPatientRecord(anyString()))
                .then(invocation -> new PatientFullRecordDto(
                        invocation.getArgument(0),
                        doctorService.getPatientPredictions(invocation.getArgument(0)),
                        doctorService.getPatientTrends(invocation.getArgument(0))
                ));
    }

    // -------------------------------------------------------------------------
    // PREDICTIONS + TRENDS
    // -------------------------------------------------------------------------

    @Test
    void getPredictions_returnsPredictionList() throws Exception {
        PredictionDto p = new PredictionDto(
                1L,
                10L,
                "HSDD",
                0.87,
                "AI explanation",
                LocalDateTime.now()
        );

        when(doctorService.getPatientPredictions("PAT-1"))
                .thenReturn(List.of(p));

        mockMvc.perform(get("/api/doctor/patients/PAT-1/predictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].symptomId").value(10))
                .andExpect(jsonPath("$[0].label").value("HSDD"))
                .andExpect(jsonPath("$[0].confidence").value(0.87))
                .andExpect(jsonPath("$[0].confidenceLevel").value("AI explanation"));
    }

    @Test
    void getTrends_returnsTrendDataList() throws Exception {
        TrendDataDto t1 = new TrendDataDto("HSDD", 5L);
        TrendDataDto t2 = new TrendDataDto("Anxiety", 2L);

        when(doctorService.getPatientTrends("PAT-2"))
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/doctor/patients/PAT-2/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("HSDD"))
                .andExpect(jsonPath("$[0].count").value(5))
                .andExpect(jsonPath("$[1].label").value("Anxiety"))
                .andExpect(jsonPath("$[1].count").value(2));
    }

    @Test
    void getPredictions_emptyListWhenNone() throws Exception {
        when(doctorService.getPatientPredictions("PAT-EMPTY"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor/patients/PAT-EMPTY/predictions"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // ANNOTATIONS
    // -------------------------------------------------------------------------

    @Test
    void getAnnotations_returnsAnnotationList() throws Exception {
        AnnotationDto dto = new AnnotationDto(
                1L,
                99L,
                "dr_smith",
                "Looks consistent with history",
                "HSDD",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(doctorService.getAnnotationsForPrediction(99L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctor/predictions/99/annotations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].predictionId").value(99))
                .andExpect(jsonPath("$[0].doctorUsername").value("dr_smith"))
                .andExpect(jsonPath("$[0].correctedLabel").value("HSDD"));
    }

    @Test
    void createAnnotation_createsAndReturnsAnnotation() throws Exception {
        AnnotationDto dto = new AnnotationDto(
                10L,
                77L,
                "doctorUser",
                "Please monitor in 2 weeks",
                "HSDD",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // doctor.annotatePrediction(...) returns an AnnotationDto
        when(mockDoctor.annotatePrediction(any(CreateAnnotationRequest.class)))
                .thenReturn(dto);

        // Factory returns our mockDoctor
        when(doctorFactory.fromUsername("doctorUser"))
                .thenReturn(mockDoctor);

        String body = """
    {
      "predictionId": 77,
      "notes": "Please monitor in 2 weeks",
      "correctedLabel": "HSDD"
    }
    """;

        mockMvc.perform(post("/api/doctor/annotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk());

        // VERIFY the correct UML-level call
        verify(mockDoctor, times(1))
                .annotatePrediction(any(CreateAnnotationRequest.class));
    }


    @Test
    void updateAnnotation_updatesAndReturnsAnnotation() throws Exception {
        AnnotationDto dto = new AnnotationDto(
                10L,
                77L,
                "doctorUser",
                "Updated notes",
                "Revised label",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(mockDoctor.editAnnotation(eq(10L), any(UpdateAnnotationRequest.class)))
                .thenReturn(dto);

        when(doctorFactory.fromUsername("doctorUser"))
                .thenReturn(mockDoctor);

        String body = """
    {
      "notes": "Updated notes",
      "correctedLabel": "Revised label"
    }
    """;

        mockMvc.perform(put("/api/doctor/annotations/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk());

        verify(mockDoctor, times(1))
                .editAnnotation(eq(10L), any(UpdateAnnotationRequest.class));
    }


    // -------------------------------------------------------------------------
    // ISSUE REPORTS
    // -------------------------------------------------------------------------

    @Test
    void reportIssue_createsIssueReport() throws Exception {
        IssueReportDto dto = new IssueReportDto(
                5L,
                88L,
                "doctorUser",
                "Model seems off for this case",
                "CorrectLabel",
                LocalDateTime.now()
        );

        when(mockDoctor.reportIssue(any(ReportIssueRequest.class)))
                .thenReturn(dto);

        when(doctorFactory.fromUsername("doctorUser"))
                .thenReturn(mockDoctor);

        String body = """
    {
      "predictionId": 88,
      "issueDescription": "Model seems off for this case",
      "correctLabel": "CorrectLabel"
    }
    """;

        mockMvc.perform(post("/api/doctor/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk());

        verify(mockDoctor, times(1))
                .reportIssue(any(ReportIssueRequest.class));
    }


    @Test
    void getIssues_returnsIssuesForPrediction() throws Exception {
        IssueReportDto dto = new IssueReportDto(
                1L,
                55L,
                "docA",
                "Ambiguous symptoms",
                "Depression",
                LocalDateTime.now()
        );

        when(doctorService.getIssuesForPrediction(55L))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctor/predictions/55/issues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].predictionId").value(55))
                .andExpect(jsonPath("$[0].issueDescription").value("Ambiguous symptoms"));
    }

    @Test
    void getIssues_emptyWhenNone() throws Exception {
        when(doctorService.getIssuesForPrediction(123L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor/predictions/123/issues"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // NOTIFICATIONS
    // -------------------------------------------------------------------------

    private User mockDoctorUser(long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setPasswordHash("hash");
        u.setRole("doctor");
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        return u;
    }

    @Test
    void getUnreadNotifications_returnsEmptyListWhenNone() throws Exception {
        User doctor = mockDoctorUser(10L, "doctor1");

        when(users.findByUsername("doctor1")).thenReturn(Optional.of(doctor));
        when(notificationService.getUnreadNotifications(10L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor/notifications")
                        .principal(() -> "doctor1"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getUnreadNotifications_throwsWhenUserNotFound() throws Exception {
        when(users.findByUsername("missingDoctor"))
                .thenReturn(Optional.empty());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/doctor/notifications")
                                .principal(() -> "missingDoctor"))
                        .andReturn()
        );

        Throwable cause = ex.getCause();
        assertTrue(cause instanceof RuntimeException);
        assertEquals("User not found", cause.getMessage());
    }

    @Test
    void markRead_marksNotificationAsRead() throws Exception {
        User doctor = mockDoctorUser(20L, "dr_mark");

        when(users.findByUsername("dr_mark")).thenReturn(Optional.of(doctor));
        doNothing().when(notificationService).markAsRead(99L, 20L);

        mockMvc.perform(post("/api/doctor/notifications/99/read")
                        .principal(() -> "dr_mark"))
                .andExpect(status().isOk());

        verify(notificationService, times(1))
                .markAsRead(99L, 20L);
    }

    // -------------------------------------------------------------------------
    // PATIENT LISTS
    // -------------------------------------------------------------------------

    @Test
    void getRecentPatients_returnsList() throws Exception {
        RecentPatientDto dto = new RecentPatientDto(
                "PAT-1",
                "Alice Smith",
                31,
                LocalDateTime.now(),
                "HSDD",
                "Follow up in one month"
        );

        when(doctorService.getRecentPatients())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctor/recent-patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value("PAT-1"))
                .andExpect(jsonPath("$[0].name").value("Alice Smith"))
                .andExpect(jsonPath("$[0].age").value(31))
                .andExpect(jsonPath("$[0].lastDiagnosis").value("HSDD"));
    }

    @Test
    void getAllPatients_returnsList() throws Exception {
        RecentPatientDto dto1 = new RecentPatientDto(
                "PAT-1",
                "Alice",
                31,
                LocalDateTime.now(),
                "Diag1",
                "Note1"
        );
        RecentPatientDto dto2 = new RecentPatientDto(
                "PAT-2",
                "Bob",
                40,
                LocalDateTime.now(),
                "Diag2",
                "Note2"
        );

        when(doctorService.getAllPatients())
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/doctor/all-patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value("PAT-1"))
                .andExpect(jsonPath("$[1].patientId").value("PAT-2"));
    }

    @Test
    void getAllPatients_emptyWhenNone() throws Exception {
        when(doctorService.getAllPatients())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor/all-patients"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // -------------------------------------------------------------------------
    // AGGREGATION: FULL PATIENT RECORD
    // -------------------------------------------------------------------------

    @Test
    void getFullRecord_returnsAggregatedRecord() throws Exception {

        String patientId = "PAT-100";

        PredictionDto p = new PredictionDto(
                1L, 10L, "HSDD", 0.9, "AI explanation", LocalDateTime.now()
        );
        TrendDataDto t = new TrendDataDto("HSDD", 3L);

        when(doctorService.getPatientPredictions(patientId))
                .thenReturn(List.of(p));
        when(doctorService.getPatientTrends(patientId))
                .thenReturn(List.of(t));

        mockMvc.perform(get("/api/doctor/patient/{patientId}/full-record", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.predictions[0].id").value(1))
                .andExpect(jsonPath("$.trends[0].label").value("HSDD"))
                .andExpect(jsonPath("$.trends[0].count").value(3));
    }

    @Test
    void getFullRecord_handlesNoPredictionsOrTrends() throws Exception {
        String patientId = "PAT-EMPTY";

        when(doctorService.getPatientPredictions(patientId))
                .thenReturn(Collections.emptyList());
        when(doctorService.getPatientTrends(patientId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor/patient/{patientId}/full-record", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.predictions").isArray())
                .andExpect(jsonPath("$.trends").isArray());
    }

    // -------------------------------------------------------------------------
    // LOW-CONFIDENCE REPORTS + FLAGS
    // -------------------------------------------------------------------------

    @Test
    void getLowConfidence_returnsList() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        LowConfidenceDto dto = new LowConfidenceDto(
                1L,
                "Alice Smith",
                "PAT-1",
                "Anxiety",
                0.3,
                now,
                "Pending Review",
                "Headache"
        );

        // if controller uses the Doctor wrapper:
        when(mockDoctor.getLowConfidenceQueue())
                .thenReturn(List.of(dto));

        // (doctorService stubs kept in case controller still calls it directly anywhere)
        when(mockDoctor.getAllLowConfidenceReports())
                .thenReturn(List.of(dto));

        when(doctorService.getLowConfidenceReports())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctor/low-confidence")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientId").value("PAT-1"))
                .andExpect(jsonPath("$[0].status").value("Pending Review"))
                .andExpect(jsonPath("$[0].symptomDescription").value("Headache"));
    }

    @Test
    void markLowConfidenceReviewed_callsServiceWithDoctorUsername() throws Exception {
        // controller now delegates through Doctor wrapper
        mockMvc.perform(put("/api/doctor/low-confidence/50/review")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk());

        verify(mockDoctor, times(1))
                .markPredictionReviewed(50L);
    }

    @Test
    void getSingleLowConfidence_returnsOne() throws Exception {
        LowConfidenceDto dto = new LowConfidenceDto(
                10L,
                "Bob",
                "PAT-2",
                "Anxiety",
                0.3,
                LocalDateTime.now(),
                "Pending Review",
                "Headache"
        );

        when(mockDoctor.getLowConfidenceDetails(10L))
                .thenReturn(dto);
        when(doctorService.getLowConfidenceReport(10L))
                .thenReturn(dto);

        mockMvc.perform(get("/api/doctor/low-confidence/10")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.patientId").value("PAT-2"))
                .andExpect(jsonPath("$.status").value("Pending Review"))
                .andExpect(jsonPath("$.symptomDescription").value("Headache"));
    }

    @Test
    void markPending_marksReportPending() throws Exception {
        mockMvc.perform(put("/api/doctor/low-confidence/33/pending")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk());

        verify(mockDoctor, times(1))
                .markPredictionPending(33L);
    }

    @Test
    void getFlagged_returnsFlaggedLowConfidence() throws Exception {
        LowConfidenceDto dto = new LowConfidenceDto(
                2L,
                "Charlie",
                "PAT-3",
                "HSDD",
                0.41,
                LocalDateTime.now(),
                "Pending Review",
                "Chest pain"
        );

        when(mockDoctor.getLowConfidenceQueue())
                .thenReturn(List.of(dto));
        when(doctorService.getLowConfidenceReports())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/doctor/flagged")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].status").value("Pending Review"))
                .andExpect(jsonPath("$[0].symptomDescription").value("Chest pain"));
    }

    // -------------------------------------------------------------------------
    // AGGREGATION: ALL REPORTS
    // -------------------------------------------------------------------------

    @Test
    void getAllReports_returnsAllReports() throws Exception {
        AllReportDto r1 = new AllReportDto(
                1L,
                "Alice",
                "PAT-1",
                "HSDD",
                0.9,
                Instant.now(),
                "dr_smith"
        );
        AllReportDto r2 = new AllReportDto(
                2L,
                "Bob",
                "PAT-2",
                "Anxiety",
                0.8,
                Instant.now(),
                "dr_jones"
        );

        // ✅ UML-consistent: controller calls Doctor → getAllReports()
        when(mockDoctor.getAllReports())
                .thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/doctor/reports")
                        .principal(() -> "doctorUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patientId").value("PAT-1"))
                .andExpect(jsonPath("$[0].disease").value("HSDD"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].patientId").value("PAT-2"))
                .andExpect(jsonPath("$[1].disease").value("Anxiety"));
    }

}
