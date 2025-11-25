package org.hsdd.dto;

public record SubmitSymptomResponse(
        SymptomDto symptom,
        PredictionDto prediction
) {}
