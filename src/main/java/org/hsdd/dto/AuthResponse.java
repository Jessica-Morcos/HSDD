package org.hsdd.dto;

public record AuthResponse(
        String token,
        String username,
        String role,
        Long userId,
        String patientId
) {}


