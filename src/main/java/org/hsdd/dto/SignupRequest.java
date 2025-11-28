package org.hsdd.dto;

public record SignupRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String dateOfBirth, // "YYYY-MM-DD"
        String phone
) {}
