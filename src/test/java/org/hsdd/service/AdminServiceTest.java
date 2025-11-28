package org.hsdd.service;

import org.hsdd.value.AuditLogEntry;
import org.hsdd.dto.AdminUserDto;
import org.hsdd.dto.AuditLogDto;
import org.hsdd.dto.CreateUserRequest;
import org.hsdd.dto.UpdateUserRequest;
import org.hsdd.repo.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminService.
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuditLogRepository auditRepo;

    @InjectMocks
    private AdminService adminService;

    // -------------------------------------------------------------------------
    // USER DELEGATION TESTS
    // -------------------------------------------------------------------------

    @Test
    void listUsers_delegatesToUserService() {
        AdminUserDto dto = new AdminUserDto(
                1L,
                "admin",
                "admin@example.com",
                "admin",
                true,
                null,
                "Admin",
                "User",
                LocalDate.of(2000, 1, 1),
                "1234567890",
                Instant.now()
        );

        when(userService.getAllUsers()).thenReturn(List.of(dto));

        List<AdminUserDto> result = adminService.listUsers();

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).username());
        verify(userService).getAllUsers();
    }

    @Test
    void getUser_delegatesToUserService() {
        AdminUserDto dto = new AdminUserDto(
                2L,
                "jess",
                "jess@example.com",
                "patient",
                true,
                "PAT-1",
                "Jess",
                "Morcos",
                LocalDate.of(2001, 5, 22),
                "1112223333",
                Instant.now()
        );

        when(userService.getUser(2L)).thenReturn(dto);

        AdminUserDto result = adminService.getUser(2L);

        assertEquals("jess", result.username());
        verify(userService).getUser(2L);
    }

    @Test
    void createUser_passesRequestAndActorCorrectly() {
        CreateUserRequest req = new CreateUserRequest(
                "newUser",
                "new@example.com",
                "Pass123!",
                "patient",
                "New",
                "User",
                "2000-01-01",
                "5556667777"
        );

        AdminUserDto created = new AdminUserDto(
                3L,
                "newUser",
                "new@example.com",
                "patient",
                true,
                "PAT-3",
                "New",
                "User",
                LocalDate.of(2000, 1, 1),
                "5556667777",
                Instant.now()
        );

        when(userService.registerUser(any(), any())).thenReturn(created);

        AdminUserDto result = adminService.createUser(req, "adminActor");

        assertEquals("newUser", result.username());

        ArgumentCaptor<CreateUserRequest> reqCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
        ArgumentCaptor<String> actorCaptor = ArgumentCaptor.forClass(String.class);

        verify(userService).registerUser(reqCaptor.capture(), actorCaptor.capture());

        CreateUserRequest sent = reqCaptor.getValue();
        assertEquals("newUser", sent.username());
        assertEquals("new@example.com", sent.email());
        assertEquals("Pass123!", sent.password());
        assertEquals("patient", sent.role());
        assertEquals("adminActor", actorCaptor.getValue());
    }

    @Test
    void updateUser_passesIdRequestAndActorCorrectly() {
        UpdateUserRequest req = new UpdateUserRequest(
                "updatedUser",
                "updated@example.com",
                "doctor",
                true,
                "Updated",
                "Name",
                "2000-02-02",
                "0001112222"
        );

        AdminUserDto updated = new AdminUserDto(
                4L,
                "updatedUser",
                "updated@example.com",
                "doctor",
                true,
                null,
                "Updated",
                "Name",
                LocalDate.of(2000, 2, 2),
                "0001112222",
                Instant.now()
        );

        when(userService.updateUser(eq(4L), any(), any())).thenReturn(updated);

        AdminUserDto result = adminService.updateUser(4L, req, "adminActor");

        assertEquals("updatedUser", result.username());

        ArgumentCaptor<UpdateUserRequest> reqCaptor = ArgumentCaptor.forClass(UpdateUserRequest.class);
        ArgumentCaptor<String> actorCaptor = ArgumentCaptor.forClass(String.class);

        verify(userService).updateUser(eq(4L), reqCaptor.capture(), actorCaptor.capture());
        assertEquals("adminActor", actorCaptor.getValue());
    }

    @Test
    void deactivateUser_callsDeleteUser() {
        adminService.deactivateUser(5L, "adminActor");
        verify(userService).deleteUser(5L, "adminActor");
    }

    @Test
    void reactivateUser_delegatesToUserService() {
        AdminUserDto dto = new AdminUserDto(
                6L,
                "reactivated",
                "react@example.com",
                "patient",
                true,
                "PAT-6",
                "Re",
                "Activated",
                LocalDate.of(1999, 9, 9),
                "9999999999",
                Instant.now()
        );

        when(userService.reactivateUser(6L, "adminActor")).thenReturn(dto);

        AdminUserDto result = adminService.reactivateUser(6L, "adminActor");

        assertEquals("reactivated", result.username());
        verify(userService).reactivateUser(6L, "adminActor");
    }

    // -------------------------------------------------------------------------
    // AUDIT AGGREGATION TESTS
    // -------------------------------------------------------------------------

    @Test
    void listAuditLogs_mapsToDtoAndAppliesLimit() {
        AuditLogEntry a1 = new AuditLogEntry();
        a1.setId(1L);
        a1.setEventTime(Instant.parse("2025-01-01T00:00:00Z"));
        a1.setActor("alice");
        a1.setEventType("LOGIN");
        a1.setDetails("desc1");
        a1.setIpAddress("1.1.1.1");

        AuditLogEntry a2 = new AuditLogEntry();
        a2.setId(2L);
        a2.setEventTime(Instant.parse("2025-01-02T00:00:00Z"));
        a2.setActor("bob");
        a2.setEventType("CREATE_USER");
        a2.setDetails("desc2");
        a2.setIpAddress("2.2.2.2");

        when(auditRepo.findAllByOrderByEventTimeDesc())
                .thenReturn(List.of(a2, a1)); // already sorted desc

        List<AuditLogDto> result = adminService.listAuditLogs(1);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals("bob", result.get(0).actor());

        verify(auditRepo).findAllByOrderByEventTimeDesc();
    }

    @Test
    void listAuditLogs_zeroLimit_returnsEmpty() {
        AuditLogEntry a = new AuditLogEntry();
        a.setId(1L);

        when(auditRepo.findAllByOrderByEventTimeDesc())
                .thenReturn(List.of(a));

        List<AuditLogDto> result = adminService.listAuditLogs(0);

        assertTrue(result.isEmpty());
    }

    @Test
    void auditSummary_groupsCountsByEventType() {
        AuditLogEntry a1 = new AuditLogEntry();
        a1.setEventType("LOGIN");

        AuditLogEntry a2 = new AuditLogEntry();
        a2.setEventType("LOGIN");

        AuditLogEntry a3 = new AuditLogEntry();
        a3.setEventType("DELETE_USER");

        when(auditRepo.findAll())
                .thenReturn(List.of(a1, a2, a3));

        Map<String, Long> result = adminService.auditSummary();

        assertEquals(2L, result.get("LOGIN"));
        assertEquals(1L, result.get("DELETE_USER"));
        assertEquals(2, result.size());

        verify(auditRepo).findAll();
    }
}
