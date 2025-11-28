package org.hsdd.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        Long predictionId,
        String message,
        boolean read,
        LocalDateTime createdAt
) {}
