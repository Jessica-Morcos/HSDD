package org.hsdd.repo;

import org.hsdd.value.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    List<Prediction> findTop200ByPatientIdOrderByCreatedAtDesc(String patientId);

    List<Prediction> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<Prediction> findByConfidenceLessThan(double threshold);
    List<Prediction> findByConfidenceLessThanAndReviewedFalse(double threshold);

    // 🔹 Used by DoctorServiceImpl
    Optional<Prediction> findTop1ByPatientIdOrderByCreatedAtDesc(String patientId);

}
