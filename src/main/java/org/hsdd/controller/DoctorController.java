package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.service.DoctorService;
import org.hsdd.service.NotificationService;
import org.hsdd.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final NotificationService notificationService;
    private final UserRepository users;

    public DoctorController(
            DoctorService doctorService,
            NotificationService notificationService,
            UserRepository users
    ) {
        this.doctorService = doctorService;
        this.notificationService = notificationService;
        this.users = users;
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
        return ResponseEntity.ok(
                doctorService.createAnnotation(req, principal.getName())
        );
    }

    @PutMapping("/annotations/{id}")
    public ResponseEntity<AnnotationDto> updateAnnotation(
            @PathVariable Long id,
            @RequestBody UpdateAnnotationRequest req,
            Principal principal
    ) {
        return ResponseEntity.ok(
                doctorService.updateAnnotation(id, req, principal.getName())
        );
    }

    // -----------------------------------
    // ISSUE REPORTS
    // -----------------------------------
    @PostMapping("/issues")
    public ResponseEntity<IssueReportDto> reportIssue(
            @RequestBody ReportIssueRequest req,
            Principal principal
    ) {
        return ResponseEntity.ok(
                doctorService.reportIssue(req, principal.getName())
        );
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
    public ResponseEntity<PatientFullRecordDto> getFullRecord(@PathVariable String patientId) {

        List<PredictionDto> predictions = doctorService.getPatientPredictions(patientId);
        List<TrendDataDto> trends = doctorService.getPatientTrends(patientId);

        PatientFullRecordDto dto = new PatientFullRecordDto(
                patientId,
                predictions,
                trends
        );

        return ResponseEntity.ok(dto);
    }


    @GetMapping("/low-confidence")
    public ResponseEntity<List<LowConfidenceDto>> getLowConfidence() {
        return ResponseEntity.ok(doctorService.getAllLowConfidenceReports());
    }



    @PutMapping("/low-confidence/{id}/review")
    public ResponseEntity<Void> markLowConfidenceReviewed(
            @PathVariable Long id,
            Principal principal
    ) {
        doctorService.markLowConfidenceReviewed(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/low-confidence/{id}")
    public ResponseEntity<LowConfidenceDto> getSingleLowConfidence(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getLowConfidenceReport(id));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AllReportDto>> getAllReports() {
        return ResponseEntity.ok(doctorService.getAllReports());
    }

    @PutMapping("/low-confidence/{id}/pending")
    public ResponseEntity<Void> markPending(
            @PathVariable Long id
    ) {
        doctorService.markLowConfidencePending(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<LowConfidenceDto>> getFlagged() {
        return ResponseEntity.ok(doctorService.getLowConfidenceReports());
    }


}
