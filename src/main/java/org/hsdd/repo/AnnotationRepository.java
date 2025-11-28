package org.hsdd.repo;

import org.hsdd.value.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    List<Annotation> findByPredictionId(Long predictionId);
    Optional<Annotation> findTop1ByPredictionIdOrderByCreatedAtDesc(Long predictionId);

    // For trend analysis (if needed later)
    List<Annotation> findByPrediction_PatientIdOrderByCreatedAtDesc(String patientId);

}
