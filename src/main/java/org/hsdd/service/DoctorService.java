package org.hsdd.service;

import org.hsdd.dto.TrendDataDto;
import org.hsdd.dto.AllReportDto;
import org.hsdd.dto.*;
import java.util.List;

public interface DoctorService {

    List<AnnotationDto> getAnnotationsForPrediction(Long predictionId);

    AnnotationDto createAnnotation(CreateAnnotationRequest req, String doctorUsername);

    AnnotationDto updateAnnotation(Long annotationId, UpdateAnnotationRequest req, String doctorUsername);

    List<IssueReportDto> getIssuesForPrediction(Long predictionId);

    IssueReportDto reportIssue(ReportIssueRequest req, String doctorUsername);

    List<PredictionDto> getPatientPredictions(String patientId);

    List<TrendDataDto> getPatientTrends(String patientId);

    List<RecentPatientDto> getRecentPatients();
    List<LowConfidenceDto> getLowConfidenceReports();
    LowConfidenceDto getLowConfidenceReport(Long id);
    List<LowConfidenceDto> getAllLowConfidenceReports();
    List<RecentPatientDto> getAllPatients();

    // ⭐ NEW: For All Reports Page
    List<AllReportDto> getAllReports();
    void markLowConfidenceReviewed(Long id, String doctorUsername);
    void markLowConfidencePending(Long id);

}
