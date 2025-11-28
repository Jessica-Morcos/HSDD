package org.hsdd.model;

import jakarta.persistence.*;
import org.hsdd.value.Prediction;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_reports")
public class IssueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;


    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Lob
    @Column(name = "issue_description", nullable = false)
    private String issueDescription;

    @Column(name = "correct_label")
    private String correctLabel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IssueReport() {}

    // -----------------------------
    // Getters & Setters
    // -----------------------------
    public Long getId() { return id; }

    public Prediction getPrediction() { return prediction; }
    public void setPrediction(Prediction prediction) { this.prediction = prediction; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getCorrectLabel() { return correctLabel; }
    public void setCorrectLabel(String correctLabel) { this.correctLabel = correctLabel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
