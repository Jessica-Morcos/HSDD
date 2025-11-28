package org.hsdd.service.impl;

import org.hsdd.model.User;
import org.hsdd.dto.*;
import org.hsdd.value.Annotation;
import org.hsdd.model.IssueReport;
import org.hsdd.value.Prediction;
import org.hsdd.value.SymptomEntry;
import org.hsdd.repo.*;
import org.hsdd.service.AuditService;
import org.hsdd.service.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final AnnotationRepository annotations;
    private final IssueReportRepository issues;
    private final PredictionRepository predictions;
    private final UserRepository users;
    private final PatientRepository patients;
    private final SymptomRepository symptoms;
    private final AuditService audit;

    public DoctorServiceImpl(
            AnnotationRepository annotations,
            IssueReportRepository issues,
            PredictionRepository predictions,
            UserRepository users,
            PatientRepository patients,
            SymptomRepository symptoms,
            AuditService audit
    ) {
        this.annotations = annotations;
        this.issues = issues;
        this.predictions = predictions;
        this.users = users;
        this.patients = patients;
        this.symptoms = symptoms;
        this.audit = audit;
    }

    // ------------------------------------
    // ANNOTATIONS
    // ------------------------------------

    @Override
    public List<AnnotationDto> getAnnotationsForPrediction(Long predictionId) {
        return annotations.findByPredictionId(predictionId)
                .stream()
                .map(this::toAnnotationDto)
                .toList();
    }

    @Override
    public AnnotationDto createAnnotation(CreateAnnotationRequest req, String doctorUsername) {
        User doctor = users.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Prediction prediction = predictions.findById(req.predictionId())
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        Annotation a = new Annotation();
        a.setDoctor(doctor);
        a.setPrediction(prediction);
        a.setNotes(req.notes());
        a.setCorrectedLabel(req.correctedLabel());

        annotations.save(a);

        audit.log(doctorUsername, "DOCTOR_CREATE_ANNOTATION",
                "predictionId=" + req.predictionId());

        return toAnnotationDto(a);
    }

    @Override
    public AnnotationDto updateAnnotation(Long annotationId, UpdateAnnotationRequest req, String doctorUsername) {
        Annotation a = annotations.findById(annotationId)
                .orElseThrow(() -> new RuntimeException("Annotation not found"));

        if (!a.getDoctor().getUsername().equals(doctorUsername)) {
            throw new RuntimeException("Unauthorized — cannot edit another doctor's annotation");
        }

        a.setNotes(req.notes());
        a.setCorrectedLabel(req.correctedLabel());
        annotations.save(a);

        audit.log(doctorUsername, "DOCTOR_UPDATE_ANNOTATION",
                "annotationId=" + annotationId);

        return toAnnotationDto(a);
    }

    // ------------------------------------
    // ISSUE REPORTS
    // ------------------------------------

    @Override
    public List<IssueReportDto> getIssuesForPrediction(Long predictionId) {
        return issues.findByPredictionId(predictionId)
                .stream()
                .map(this::toIssueDto)
                .toList();
    }

    @Override
    public IssueReportDto reportIssue(ReportIssueRequest req, String doctorUsername) {
        User doctor = users.findByUsername(doctorUsername)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Prediction prediction = predictions.findById(req.predictionId())
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        IssueReport issue = new IssueReport();
        issue.setDoctor(doctor);
        issue.setPrediction(prediction);
        issue.setIssueDescription(req.issueDescription());
        issue.setCorrectLabel(req.correctLabel());

        issues.save(issue);

        audit.log(doctorUsername, "DOCTOR_REPORT_ISSUE",
                "predictionId=" + req.predictionId());

        return toIssueDto(issue);
    }

    // ------------------------------------
    // PATIENT RECORDS
    // ------------------------------------

    @Override
    public List<PredictionDto> getPatientPredictions(String patientId) {
        return predictions.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::toPredictionDto)
                .toList();
    }

    @Override
    public List<TrendDataDto> getPatientTrends(String patientId) {
        return predictions.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(Prediction::getLabel,
                        java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .map(e -> new TrendDataDto(e.getKey(), e.getValue()))
                .toList();
    }

    // ------------------------------------
    // RECENT PATIENTS
    // ------------------------------------

    @Override
    public List<RecentPatientDto> getRecentPatients() {

        return patients.findAll().stream().map(p -> {

                    // fetch last symptom or prediction timestamps
                    LocalDateTime lastSymptom = symptoms
                            .findTop1ByPatientIdOrderBySubmittedAtDesc(p.getPatientId())
                            .map(SymptomEntry::getSubmittedAt)
                            .orElse(null);

                    Prediction lastPredictionEntity = predictions
                            .findTop1ByPatientIdOrderByCreatedAtDesc(p.getPatientId())
                            .orElse(null);

                    LocalDateTime lastPredictionTime =
                            lastPredictionEntity != null ? lastPredictionEntity.getCreatedAt() : null;

                    LocalDateTime lastVisit = latest(lastSymptom, lastPredictionTime);

                    String lastDiagnosis =
                            lastPredictionEntity != null ? lastPredictionEntity.getLabel() : "No diagnosis yet";

                    // ⭐ NEW: fetch latest annotation notes if exists
                    String doctorNotes = "—";
                    if (lastPredictionEntity != null) {
                        var latestAnnotation = annotations
                                .findTop1ByPredictionIdOrderByCreatedAtDesc(lastPredictionEntity.getId());

                        if (latestAnnotation.isPresent()) {
                            doctorNotes = latestAnnotation.get().getNotes();
                        }
                    }

                    return new RecentPatientDto(
                            p.getPatientId(),
                            p.getFirstName() + " " + p.getLastName(),
                            calculateAge(p.getDateOfBirth()),
                            lastVisit,
                            lastDiagnosis,
                            doctorNotes
                    );
                })
                .sorted((a, b) -> {
                    if (a.lastVisit() == null && b.lastVisit() == null) return 0;
                    if (a.lastVisit() == null) return 1;
                    if (b.lastVisit() == null) return -1;
                    return b.lastVisit().compareTo(a.lastVisit());
                })
                .toList();
    }



    private LocalDateTime latest(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private Integer calculateAge(LocalDate dob) {
        if (dob == null) return null;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    // ------------------------------------
    // LOW CONFIDENCE REPORTS
    // ------------------------------------

    @Override
    public List<LowConfidenceDto> getLowConfidenceReports() {
        return predictions.findByConfidenceLessThanAndReviewedFalse(0.55)
                .stream()
                .map(p -> {
                    String patientName = patients.findByPatientId(p.getPatientId())
                            .map(pp -> pp.getFirstName() + " " + pp.getLastName())
                            .orElse("Unknown");

                    SymptomEntry s = symptoms.findById(p.getSymptomId()).orElse(null);
                    String symptomText = (s != null) ? s.getDescription() : "—";

                    return new LowConfidenceDto(
                            p.getId(),
                            patientName,
                            p.getPatientId(),
                            p.getLabel(),
                            p.getConfidence(),
                            p.getCreatedAt(),
                            "Pending Review",       // <-- EXACTLY LIKE ORIGINAL
                            symptomText
                    );
                })
                .toList();
    }





    @Override
    public LowConfidenceDto getLowConfidenceReport(Long id) {
        Prediction p = predictions.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        String patientName = patients.findByPatientId(p.getPatientId())
                .map(pp -> pp.getFirstName() + " " + pp.getLastName())
                .orElse("Unknown");

        SymptomEntry s = symptoms.findById(p.getSymptomId()).orElse(null);
        String symptomText = (s != null) ? s.getDescription() : "—";

        return new LowConfidenceDto(
                p.getId(),
                patientName,
                p.getPatientId(),
                p.getLabel(),
                p.getConfidence(),
                p.getCreatedAt(),
                p.isReviewed() ? "Reviewed" : "Pending Review",
                symptomText
        );
    }



    @Override
    @Transactional
    public void markLowConfidenceReviewed(Long id, String doctorUsername) {
        Prediction p = predictions.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        p.setReviewed(true);

        // persist the change
        predictions.save(p);

        // ✅ tests expect this audit call
        audit.log(
                doctorUsername,
                "DOCTOR_REVIEW_LOW_CONFIDENCE",
                "predictionId=" + id
        );
    }



    // ------------------------------------
    // GET ALL REPORTS (FOR AllReportsPage)
    // ------------------------------------

    @Override
    public List<AllReportDto> getAllReports() {
        return predictions.findAll().stream()
                .map(p -> {

                    // lookup patient by patientId
                    String patientName = patients.findByPatientId(p.getPatientId())
                            .map(pp -> pp.getFirstName() + " " + pp.getLastName())
                            .orElse("Unknown");

                    return new AllReportDto(
                            p.getId(),
                            patientName,
                            p.getPatientId(),
                            p.getLabel(),
                            p.getConfidence(),
                            p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                            "Dr. Carter"   // placeholder until doctor info is added
                    );
                })
                .toList();
    }


    @Override
    public List<LowConfidenceDto> getAllLowConfidenceReports() {
        return predictions.findByConfidenceLessThan(0.55)
                .stream()
                .map(p -> {
                    String patientName = patients.findByPatientId(p.getPatientId())
                            .map(pp -> pp.getFirstName() + " " + pp.getLastName())
                            .orElse("Unknown");

                    SymptomEntry s = symptoms.findById(p.getSymptomId()).orElse(null);
                    String symptomText = (s != null) ? s.getDescription() : "—";

                    return new LowConfidenceDto(
                            p.getId(),
                            patientName,
                            p.getPatientId(),
                            p.getLabel(),
                            p.getConfidence(),
                            p.getCreatedAt(),
                            p.isReviewed() ? "Reviewed" : "Pending Review",
                            symptomText
                    );
                })
                .toList();
    }


    @Override
    public List<RecentPatientDto> getAllPatients() {

        return patients.findAll().stream().map(p -> {

                    LocalDateTime lastSymptom = symptoms
                            .findTop1ByPatientIdOrderBySubmittedAtDesc(p.getPatientId())
                            .map(SymptomEntry::getSubmittedAt)
                            .orElse(null);

                    Prediction lastPrediction = predictions
                            .findTop1ByPatientIdOrderByCreatedAtDesc(p.getPatientId())
                            .orElse(null);

                    LocalDateTime lastPredictionTime =
                            lastPrediction != null ? lastPrediction.getCreatedAt() : null;

                    LocalDateTime lastVisit = latest(lastSymptom, lastPredictionTime);

                    String lastDiagnosis =
                            lastPrediction != null ? lastPrediction.getLabel() : "No diagnosis yet";

                    String doctorNotes = "—";
                    if (lastPrediction != null) {
                        var latestAnnotation = annotations
                                .findTop1ByPredictionIdOrderByCreatedAtDesc(lastPrediction.getId());

                        if (latestAnnotation.isPresent()) {
                            doctorNotes = latestAnnotation.get().getNotes();
                        }
                    }

                    return new RecentPatientDto(
                            p.getPatientId(),
                            p.getFirstName() + " " + p.getLastName(),
                            calculateAge(p.getDateOfBirth()),
                            lastVisit,
                            lastDiagnosis,
                            doctorNotes
                    );
                })
                .toList();
    }

    // ------------------------------------
    // DTO MAPPERS
    // ------------------------------------

    private AnnotationDto toAnnotationDto(Annotation a) {
        return new AnnotationDto(
                a.getId(),
                a.getPrediction().getId(),
                a.getDoctor().getUsername(),
                a.getNotes(),
                a.getCorrectedLabel(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    private IssueReportDto toIssueDto(IssueReport r) {
        return new IssueReportDto(
                r.getId(),
                r.getPrediction().getId(),
                r.getDoctor().getUsername(),
                r.getIssueDescription(),
                r.getCorrectLabel(),
                r.getCreatedAt()
        );
    }

    private PredictionDto toPredictionDto(Prediction p) {

        double conf = p.getConfidence();

        String level;
        if (conf >= 0.90) {
            level = "high";
        } else if (conf >= 0.75) {
            level = "moderate";
        } else {
            level = "low";
        }

        return new PredictionDto(
                p.getId(),
                p.getSymptomId(),
                p.getLabel(),
                p.getConfidence(),
                level,          // ⭐ NEW REQUIRED FIELD
                p.getCreatedAt()
        );
    }


    @Override
    @Transactional
    public void markLowConfidencePending(Long id) {
        Prediction p = predictions.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found"));

        p.setReviewed(false);

        predictions.save(p);  // 🔥 ALSO REQUIRED
    }

    // ----------------------------------------------------
// Helper: map reviewed flag → status string
// ----------------------------------------------------
    private String statusFromReviewed(boolean reviewed) {
        return reviewed ? "Reviewed" : "Pending Review";
    }

}
