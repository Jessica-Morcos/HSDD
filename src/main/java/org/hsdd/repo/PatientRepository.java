package org.hsdd.repo;

import org.hsdd.model.Patient;
import org.hsdd.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByPatientId(String patientId);


    Optional<Patient> findByUser(User user);

    Optional<Patient> findByUser_Username(String username);
    // new version – used when submitting symptoms
    Optional<Patient> findByPatientId(String patientId);
}
