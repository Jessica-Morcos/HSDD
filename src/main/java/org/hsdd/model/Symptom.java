package org.hsdd.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "symptoms", schema = "HSDD")
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    // DB column is TEXT named "description"
    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    // DB column is JSON; we store raw JSON string and map in service
    @Column(name = "tags", columnDefinition = "json")
    private String tags;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public Symptom() {}

    public Symptom(String patientId, String text, String tagsJson) {
        this.patientId = patientId;
        this.description = text;
        this.tags = tagsJson;
        this.submittedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}