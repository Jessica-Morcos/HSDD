package org.hsdd.dto;

public record UpdateAnnotationRequest(
        String notes,
        String correctedLabel
) {}
