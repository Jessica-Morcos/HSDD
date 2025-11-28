package org.hsdd.dto;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        Instant eventTime,
        String actor,
        String eventType,
        String details,
        String ipAddress
) {}
