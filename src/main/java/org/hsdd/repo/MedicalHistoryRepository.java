package org.hsdd.repo;

import org.hsdd.model.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {

    List<MedicalHistory> findByPatientIdOrderByDiagnosedAtDesc(String patientId);
}