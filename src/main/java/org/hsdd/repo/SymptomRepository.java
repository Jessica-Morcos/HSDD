package org.hsdd.repo;

import org.hsdd.value.SymptomEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SymptomRepository extends JpaRepository<SymptomEntry, Long> {

    // Recent symptoms for patient
    List<SymptomEntry> findTop200ByPatientIdOrderBySubmittedAtDesc(String patientId);

    // 🔹 Used by DoctorServiceImpl
    Optional<SymptomEntry> findTop1ByPatientIdOrderBySubmittedAtDesc(String patientId);
}
