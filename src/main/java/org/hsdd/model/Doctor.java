package org.hsdd.model;

import org.hsdd.dto.AllReportDto;
import org.hsdd.dto.AnnotationDto;
import org.hsdd.dto.CreateAnnotationRequest;
import org.hsdd.dto.IssueReportDto;
import org.hsdd.dto.LowConfidenceDto;
import org.hsdd.dto.PatientFullRecordDto;
import org.hsdd.dto.PredictionDto;
import org.hsdd.dto.PredictionWithAnnotationDto;
import org.hsdd.dto.RecentPatientDto;
import org.hsdd.dto.ReportIssueRequest;
import org.hsdd.dto.SymptomDto;
import org.hsdd.dto.TrendDataDto;
import org.hsdd.dto.UpdateAnnotationRequest;
import org.hsdd.service.DoctorService;
import org.hsdd.service.RecordsService;

import java.util.List;

public class Doctor {

    private final User user;
    private final DoctorService doctorService;
    private final RecordsService recordsService;

    public Doctor(User user,
                  DoctorService doctorService,
                  RecordsService recordsService) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.user = user;
        this.doctorService = doctorService;
        this.recordsService = recordsService;
    }


    public Long getId() {
        return user.getId();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getRole() {
        return user.getRole();
    }

    public boolean isActive() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }




    public PatientFullRecordDto viewPatientRecord(String patientId) {
        List<PredictionDto> predictions =
                doctorService.getPatientPredictions(patientId);

        List<TrendDataDto> trends =
                doctorService.getPatientTrends(patientId);

        return new PatientFullRecordDto(
                patientId,
                predictions,
                trends
        );
    }


    public List<SymptomDto> viewPatientSymptoms(String patientId) {
        return recordsService.listSymptoms(patientId);
    }


    public List<PredictionWithAnnotationDto> viewPredictionHistory(String patientId) {
        return recordsService.listPredictions(patientId);
    }

    // -----------------------------
    // Annotations
    // -----------------------------


    public AnnotationDto annotatePrediction(CreateAnnotationRequest req) {
        return doctorService.createAnnotation(req, user.getUsername());
    }

    public AnnotationDto editAnnotation(Long annotationId, UpdateAnnotationRequest req) {
        return doctorService.updateAnnotation(annotationId, req, user.getUsername());
    }

    // -----------------------------
    // Issue reports
    // -----------------------------


    public IssueReportDto reportIssue(ReportIssueRequest req) {
        return doctorService.reportIssue(req, user.getUsername());
    }



    public List<LowConfidenceDto> getLowConfidenceQueue() {

        return doctorService.getLowConfidenceReports();
    }

    public List<LowConfidenceDto> getAllLowConfidenceReports() {

        return doctorService.getAllLowConfidenceReports();
    }


    public LowConfidenceDto getLowConfidenceDetails(Long predictionId) {
        return doctorService.getLowConfidenceReport(predictionId);
    }

    public void markPredictionReviewed(Long predictionId) {
        doctorService.markLowConfidenceReviewed(predictionId, user.getUsername());
    }

    public void markPredictionPending(Long predictionId) {
        doctorService.markLowConfidencePending(predictionId);
    }

    // -----------------------------
    // Patients & reports
    // -----------------------------

    public List<RecentPatientDto> getRecentPatients() {
        return doctorService.getRecentPatients();
    }

    public List<RecentPatientDto> getAllPatients() {
        return doctorService.getAllPatients();
    }

    public List<AllReportDto> getAllReports() {
        return doctorService.getAllReports();
    }
}
