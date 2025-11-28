package org.hsdd.dto;

import java.time.LocalDateTime;

public record PredictionWithAnnotationDto(
        Long id,
        Long symptomId,
        String label,
        Double confidence,
        String confidenceLevel,
        LocalDateTime createdAt,
        String doctorUsername,
        String doctorNotes,
        String correctedLabel
) {}
