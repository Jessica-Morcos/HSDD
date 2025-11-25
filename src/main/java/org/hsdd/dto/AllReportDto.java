package org.hsdd.dto;

import java.time.Instant;

public record AllReportDto(
        Long id,
        String patientName,
        String patientId,
        String disease,
        Double confidence,
        Instant createdAt,
        String doctor
) {}
