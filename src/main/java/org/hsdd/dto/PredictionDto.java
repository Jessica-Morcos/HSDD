package org.hsdd.dto;

import java.time.LocalDateTime;

public record PredictionDto(
        Long id,
        Long symptomId,
        String label,
        Double confidence,
        String confidenceLevel,
        LocalDateTime createdAt
) {}
