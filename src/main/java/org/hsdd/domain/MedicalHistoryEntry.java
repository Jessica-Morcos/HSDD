package org.hsdd.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "medical_history", schema = "HSDD")
@Getter
@Setter
@NoArgsConstructor
public class MedicalHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", length = 8, nullable = false)
    private String patientId;          // matches HSDD.medical_history.patient_id

    @Column(name = "title", length = 100, nullable = false)
    private String title;              // e.g. "Asthma"

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;            // e.g. "uses inhaler daily"

    @Column(name = "diagnosed_at", nullable = false,
            updatable = false, insertable = false)
    private Instant diagnosedAt;       // filled by DB DEFAULT CURRENT_TIMESTAMP
}