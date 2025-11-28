package org.hsdd.dto;

import java.time.LocalDateTime;


import java.time.LocalDateTime;

public record LowConfidenceDto(
        Long id,
        String patientName,
        String patientId,
        String predictedDisease,
        double confidence,
        LocalDateTime submittedOn,
        String status,
        String symptomDescription   // ⭐ NEW FIELD
) {}




