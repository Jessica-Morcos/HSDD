package org.hsdd.repo;

import org.hsdd.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {

    // Recent symptoms for patient
    List<Symptom> findTop200ByPatientIdOrderBySubmittedAtDesc(String patientId);

    // 🔹 Used by DoctorServiceImpl
    Optional<Symptom> findTop1ByPatientIdOrderBySubmittedAtDesc(String patientId);
}
