package org.hsdd.dto;

public record CreateUserRequest(
        String username,
        String email,
        String password,
        String role,       // "patient", "doctor", "admin"
        String firstName,
        String lastName,
        String dateOfBirth,   // ISO-8601, e.g. "1995-05-21"
        String phone
) {}
