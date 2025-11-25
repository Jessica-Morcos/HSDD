package org.hsdd.dto;

import java.util.List;

public record PatientFullRecordDto(
        String patientId,
        List<PredictionDto> predictions,
        List<TrendDataDto> trends
) {}
