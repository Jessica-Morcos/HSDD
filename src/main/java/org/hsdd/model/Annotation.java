package org.hsdd.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hsdd.domain.User;
import org.hsdd.model.Prediction;

@Entity
@Table(name = "annotations")
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to prediction
    @ManyToOne(optional = false)
    @JoinColumn(name = "prediction_id")
    private Prediction prediction;

    // Link to doctor user (role = DOCTOR)
    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Lob
    @Column(nullable = false)
    private String notes;

    @Column(name = "corrected_label")
    private String correctedLabel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Annotation() {}

    // Getters & setters
    public Long getId() { return id; }
    public Prediction getPrediction() { return prediction; }
    public void setPrediction(Prediction prediction) { this.prediction = prediction; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCorrectedLabel() { return correctedLabel; }
    public void setCorrectedLabel(String correctedLabel) { this.correctedLabel = correctedLabel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
