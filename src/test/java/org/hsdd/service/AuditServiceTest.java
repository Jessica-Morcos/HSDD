package org.hsdd.service;

import jakarta.servlet.http.HttpServletRequest;
import org.hsdd.domain.AuditLog;
import org.hsdd.repo.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository repo;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditService auditService;

    // -----------------------------------------------------------
    // 1. BASIC SUCCESS CASE — all fields set + saved
    // -----------------------------------------------------------
    @Test
    void log_savesCorrectAuditLog() {
        when(request.getRemoteAddr()).thenReturn("111.222.333.444");

        auditService.log("jess", "LOGIN", "User logged in");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo).save(captor.capture());

        AuditLog saved = captor.getValue();

        assertEquals("jess", saved.getActor());
        assertEquals("LOGIN", saved.getEventType());
        assertEquals("User logged in", saved.getDetails());
        assertEquals("111.222.333.444", saved.getIpAddress());
    }

    // -----------------------------------------------------------
    // 2. NULL ACTOR — ensure null actor is allowed and saved
    // -----------------------------------------------------------
    @Test
    void log_allowsNullActor() {
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");

        auditService.log(null, "UPDATE", "Updated user");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo).save(captor.capture());

        AuditLog saved = captor.getValue();

        assertNull(saved.getActor());
        assertEquals("UPDATE", saved.getEventType());
        assertEquals("Updated user", saved.getDetails());
        assertEquals("9.9.9.9", saved.getIpAddress());
    }

    // -----------------------------------------------------------
    // 3. REQUEST IP HANDLING — verify IP is always taken from request
    // -----------------------------------------------------------
    @Test
    void log_usesRequestIpEveryTime() {
        when(request.getRemoteAddr())
                .thenReturn("5.5.5.5")
                .thenReturn("6.6.6.6"); // second call

        auditService.log("alice", "DELETE", "Deleted account");
        auditService.log("bob", "CREATE", "Created account");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo, times(2)).save(captor.capture());

        AuditLog first = captor.getAllValues().get(0);
        AuditLog second = captor.getAllValues().get(1);

        assertEquals("5.5.5.5", first.getIpAddress());
        assertEquals("6.6.6.6", second.getIpAddress());
    }
}
