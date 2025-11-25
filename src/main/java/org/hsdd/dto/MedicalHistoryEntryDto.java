package org.hsdd.dto;

import org.hsdd.domain.MedicalHistoryEntry;

import java.time.Instant;

public record MedicalHistoryEntryDto(
        Long id,
        String title,
        String details,
        Instant diagnosedAt
) {
    public static MedicalHistoryEntryDto fromEntity(MedicalHistoryEntry e) {
        return new MedicalHistoryEntryDto(
                e.getId(),
                e.getTitle(),
                e.getDetails(),
                e.getDiagnosedAt()
        );
    }
}