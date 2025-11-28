package org.hsdd.value;

import jakarta.persistence.*;
import org.hsdd.model.IssueReport;
import org.hsdd.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "predictions", schema = "HSDD")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 8)
    private String patientId;

    // Keep the primitive ID (NO DB CHANGES)
    @Column(name = "symptom_id", nullable = false)
    private Long symptomId;

    // Link back to SymptomEntry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id", insertable = false, updatable = false)
    private SymptomEntry symptomEntity;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Annotation> annotations = new ArrayList<>();

    @OneToMany(mappedBy = "prediction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IssueReport> issueReports = new ArrayList<>();

    // ------------------------------------------------------
    // Constructors
    // ------------------------------------------------------
    public Prediction() {}

    public Prediction(String patientId, Long symptomId, String label, Double confidence) {
        this.patientId = patientId;
        this.symptomId = symptomId;
        this.label = label;
        this.confidence = confidence;
        this.createdAt = LocalDateTime.now();
    }

    // ------------------------------------------------------
    // Getters / Setters
    // ------------------------------------------------------
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getPatientId() { return patientId; }

    public void setPatientId(String patientId) { this.patientId = patientId; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private User doctor;


    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }


    public Long getSymptomId() { return symptomId; }

    public void setSymptomId(Long symptomId) { this.symptomId = symptomId; }



    public SymptomEntry getSymptomEntity() {
        return symptomEntity;
    }

    public void setSymptomEntity(SymptomEntry symptomEntity) {
        this.symptomEntity = symptomEntity;
    }


    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public double getConfidence() { return confidence; }

    public void setConfidence(double confidence) { this.confidence = confidence; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isReviewed() { return reviewed; }

    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    public List<Annotation> getAnnotations() { return annotations; }

    public void setAnnotations(List<Annotation> annotations) { this.annotations = annotations; }

    public List<IssueReport> getIssueReports() { return issueReports; }

    public void setIssueReports(List<IssueReport> issueReports) { this.issueReports = issueReports; }
}
