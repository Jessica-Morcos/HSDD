package org.hsdd.repo;

import org.hsdd.domain.MedicalHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistoryEntry, Long> {

    List<MedicalHistoryEntry> findByPatientIdOrderByDiagnosedAtDesc(String patientId);
}