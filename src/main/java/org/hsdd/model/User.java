package org.hsdd.model;

import jakarta.persistence.*;
import org.hsdd.value.Prediction;
import org.hsdd.model.IssueReport;
import org.hsdd.value.Annotation;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 191)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, length = 20)
    private String role = "patient";

    @Column(nullable = false)
    private boolean active = true;

    // ----------------------------------------------------------
    // UML RELATIONSHIPS
    // ----------------------------------------------------------

    // 1. User ↔ Patient (1–1)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Patient patient;

    // 2. User ↔ Predictions (doctor side) (1–*)
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Prediction> doctorPredictions;

    // 3. User ↔ IssueReports (doctor side)
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<IssueReport> issueReports;

    // 4. User ↔ Annotations (doctor side)
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Annotation> annotations;

    // ----------------------------------------------------------
    // getters / setters
    // ----------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Patient getPatient() { return patient; }

    public List<Prediction> getDoctorPredictions() { return doctorPredictions; }

    public List<IssueReport> getIssueReports() { return issueReports; }

    public List<Annotation> getAnnotations() { return annotations; }
}
