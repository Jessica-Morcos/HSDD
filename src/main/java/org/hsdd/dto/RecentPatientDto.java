package org.hsdd.dto;

import java.time.LocalDateTime;

public record RecentPatientDto(
        String patientId,
        String name,
        Integer age,
        LocalDateTime lastVisit,
        String lastDiagnosis,
        String doctorNotes
) {}

