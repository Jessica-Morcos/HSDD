package org.hsdd.dto;

public record ReportIssueRequest(
        Long predictionId,
        String issueDescription,
        String correctLabel
) {}
