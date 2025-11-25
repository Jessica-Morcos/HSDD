package org.hsdd.dto;

import java.time.LocalDateTime;

public record AnnotationDto(
        Long id,
        Long predictionId,
        String doctorUsername,
        String notes,
        String correctedLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
