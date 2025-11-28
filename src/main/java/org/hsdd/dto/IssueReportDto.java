package org.hsdd.dto;

import java.time.LocalDateTime;

public record IssueReportDto(
        Long id,
        Long predictionId,
        String doctorUsername,
        String issueDescription,
        String correctLabel,
        LocalDateTime createdAt
) {}
