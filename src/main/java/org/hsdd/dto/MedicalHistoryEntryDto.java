package org.hsdd.dto;

import org.hsdd.model.MedicalHistory;

import java.time.Instant;

public record MedicalHistoryEntryDto(
        Long id,
        String title,
        String details,
        Instant diagnosedAt
) {
    public static MedicalHistoryEntryDto fromEntity(MedicalHistory e) {
        return new MedicalHistoryEntryDto(
                e.getId(),
                e.getTitle(),
                e.getDetails(),
                e.getDiagnosedAt()
        );
    }
}