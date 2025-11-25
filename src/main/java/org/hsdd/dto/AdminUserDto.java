package org.hsdd.dto;

import java.time.Instant;
import java.time.LocalDate;

public record AdminUserDto(
        Long userId,
        String username,
        String email,
        String role,
        Boolean active,
        String patientId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        Instant createdAt
) {}
