package org.hsdd.value;

import java.time.LocalDateTime;

public class SessionToken {

    private final String token;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private final long ttlMinutes = 60; // UML-only metadata

    public SessionToken(String token) {
        this.token = token;
    }

    public String getToken() { return token; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return createdAt.plusMinutes(ttlMinutes)
                .isBefore(LocalDateTime.now());
    }
}
