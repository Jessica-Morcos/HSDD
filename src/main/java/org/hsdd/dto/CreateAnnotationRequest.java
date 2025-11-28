package org.hsdd.dto;

public record CreateAnnotationRequest(
        Long predictionId,
        String notes,
        String correctedLabel
) {}
