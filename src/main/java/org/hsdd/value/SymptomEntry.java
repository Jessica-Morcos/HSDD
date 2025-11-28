package org.hsdd.value;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "symptoms", schema = "HSDD")
public class SymptomEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "tags", columnDefinition = "json")
    private String tags;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    // ----------------------------------------------------------
    // ✔ UML Aggregation: SymptomEntity → Predictions
    // ----------------------------------------------------------
    // This matches Prediction.symptomEntity (the ManyToOne side)
    @OneToMany(mappedBy = "symptomEntity", fetch = FetchType.LAZY)
    private List<Prediction> predictions = new ArrayList<>();

    // ----------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------
    public SymptomEntry() {}

    public SymptomEntry(String patientId, String text, String tagsJson) {
        this.patientId = patientId;
        this.description = text;
        this.tags = tagsJson;
        this.submittedAt = LocalDateTime.now();
    }

    // ----------------------------------------------------------
    // Getters / Setters
    // ----------------------------------------------------------
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getPatientId() { return patientId; }

    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }

    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public List<Prediction> getPredictions() { return predictions; }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }
    // Returns a truncated summary of the symptom description
    public String getSummary() {
        if (description == null) {
            return "";
        }
        int limit = 80;  // adjust if UML specifies a different limit
        return description.length() <= limit
                ? description
                : description.substring(0, limit - 3) + "...";
    }

    // Attaches this symptom to a patient (UML operation)
    public void attachToPatient(String patientId) {
        this.patientId = patientId;
    }
}
