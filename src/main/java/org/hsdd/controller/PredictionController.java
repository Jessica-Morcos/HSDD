package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.model.Doctor;
import org.hsdd.service.DoctorFactory;
import org.hsdd.service.DoctorService;
import org.hsdd.service.NotificationService;
import org.hsdd.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class PredictionController {

    private final DoctorService doctorService;
    private final NotificationService notificationService;
    private final UserRepository users;
    private final DoctorFactory doctorFactory;

    public PredictionController(
            DoctorService doctorService,
            NotificationService notificationService,
            UserRepository users,
            DoctorFactory doctorFactory
    ) {
        this.doctorService = doctorService;
        this.notificationService = notificationService;
        this.users = users;
        this.doctorFactory = doctorFactory;
    }

    // -----------------------------------
    // PREDICTIONS
    // -----------------------------------
    @GetMapping("/patients/{patientId}/predictions")
    public ResponseEntity<List<PredictionDto>> getPredictions(@PathVariable String patientId) {
        return ResponseEntity.ok(doctorService.getPatientPredictions(patientId));
    }

    @GetMapping("/patients/{patientId}/trends")
    public ResponseEntity<List<TrendDataDto>> getTrends(@PathVariable String patientId) {
        return ResponseEntity.ok(doctorService.getPatientTrends(patientId));
    }

    // -----------------------------------
    // ANNOTATIONS
    // -----------------------------------
    @GetMapping("/predictions/{predictionId}/annotations")
    public ResponseEntity<List<AnnotationDto>> getAnnotations(@PathVariable Long predictionId) {
        return ResponseEntity.ok(doctorService.getAnnotationsForPrediction(predictionId));
    }

    @PostMapping("/annotations")
    public ResponseEntity<AnnotationDto> createAnnotation(
            @RequestBody CreateAnnotationRequest req,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.annotatePrediction(req));
    }

    @PutMapping("/annotations/{id}")
    public ResponseEntity<AnnotationDto> updateAnnotation(
            @PathVariable Long id,
            @RequestBody UpdateAnnotationRequest req,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.editAnnotation(id, req));
    }

    // -----------------------------------
    // ISSUE REPORTS
    // -----------------------------------
    @PostMapping("/issues")
    public ResponseEntity<IssueReportDto> reportIssue(
            @RequestBody ReportIssueRequest req,
            Principal principal
    ) {
        Doctor doctor = doctorFactory.fromUsername(principal.getName());
        return ResponseEntity.ok(doctor.reportIssue(req));
    }

    // -----------------------------------
    // NOTIFICATIONS
    // -----------------------------------
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getUnread(Principal principal) {
        Long userId = users.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        List<NotificationDto> dtos = notificationService
                .getUnreadNotifications(userId)
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getPrediction().getId(),
                        n.getMessage(),
                        n.isReadFlag(),
                        n.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            Principal principal
    ) {
        Long userId = users.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/predictions/{predictionId}/issues")
    public ResponseEntity<List<IssueReportDto>> getIssues(
            @PathVariable Long predictionId
    ) {
        return ResponseEntity.ok(
                doctorService.getIssuesForPrediction(predictionId)
        );
    }

    @GetMapping("/recent-patients")
    public ResponseEntity<List<RecentPatientDto>> getRecentPatients() {
        return ResponseEntity.ok(doctorService.getRecentPatients());
    }

    @GetMapping("/all-patients")
    public ResponseEntity<List<RecentPatientDto>> getAllPatients() {
        return ResponseEntity.ok(doctorService.getAllPatients());
    }

    @GetMapping("/patient/{patientId}/full-record")
    public ResponseEntity<PatientFullRecordDto> getFullRecord(
            @PathVariable String patientId,
            Principal principal
    ) {
        String username = (principal != null) ? principal.getName() : null;

        Doctor doctor = doctorFactory.fromUsername(username);
        PatientFullRecordDto dto = doctor.viewPatientRecord(patientId);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AllReportDto>> getAllReports(Principal principal) {

        // Allow tests without principal
        String username = (principal != null) ? principal.getName() : null;

        Doctor doctor = doctorFactory.fromUsername(username);
        return ResponseEntity.ok(doctor.getAllReports());
    }

}
