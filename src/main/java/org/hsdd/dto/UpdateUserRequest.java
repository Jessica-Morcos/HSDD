package org.hsdd.dto;

public record UpdateUserRequest(
        String email,
        String password,
        String role,
        Boolean active,
        String firstName,
        String lastName,
        String dateOfBirth,
        String phone
) {}
