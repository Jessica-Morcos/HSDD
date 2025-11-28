package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplianceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UserRepository userRepository;

    // =======================================================================================
    // 1) listUsers()  → returns JSON array of AdminUserDto
    // =======================================================================================
    @Test
    void listUsers_returnsListOfUsers() throws Exception {

        AdminUserDto dto = new AdminUserDto(
                10L,
                "jess",
                "jess@example.com",
                "ADMIN",
                true,
                "PAT-1",
                "Jess",
                "Morcos",
                LocalDate.of(2001, 5, 22),
                "1234567890",
                Instant.now()
        );

        when(adminService.listUsers()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].username").value("jess"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    // =======================================================================================
    // 2) getUser(id) → returns a single AdminUserDto
    // =======================================================================================
    @Test
    void getUser_returnsSingleUser() throws Exception {

        AdminUserDto dto = new AdminUserDto(
                5L,
                "maria",
                "maria@example.com",
                "USER",
                true,
                "PAT-55",
                "Maria",
                "Lopez",
                LocalDate.of(1999, 3, 12),
                "5550009999",
                Instant.now()
        );

        when(adminService.getUser(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.username").value("maria"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    // =======================================================================================
    // 3) systemHealth() → test activeUsers + required fields
    // =======================================================================================
    @Test
    void systemHealth_returnsHealthJson() throws Exception {

        when(userRepository.countByActiveTrue()).thenReturn(7L);

        mockMvc.perform(get("/api/admin/system-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(7))
                .andExpect(jsonPath("$.cpuUsage").exists())
                .andExpect(jsonPath("$.memoryUsage").exists())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.lastBackup").value("2025-11-11 23:45"));
    }
    @Test
    void createUser_returnsCreatedUser() throws Exception {

        AdminUserDto dto = new AdminUserDto(
                20L, "newUser", "new@example.com", "USER",
                true, "PAT-99", "New", "User",
                LocalDate.of(2000, 1, 1),
                "1112223333", Instant.now()
        );

        when(adminService.createUser(any(CreateUserRequest.class), eq("admin")))
                .thenReturn(dto);

        String body = """
        {
            "username":"newUser",
            "email":"new@example.com",
            "password":"Password123!",
            "role":"USER"
        }
        """;

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(20))
                .andExpect(jsonPath("$.username").value("newUser"));
    }

    @Test
    void updateUser_updatesAndReturnsUser() throws Exception {

        AdminUserDto dto = new AdminUserDto(
                50L, "updated", "up@example.com", "USER",
                true, "PAT-50", "Up", "Dated",
                LocalDate.of(1990, 2, 2),
                "2223334444", Instant.now()
        );

        when(adminService.updateUser(eq(50L), any(UpdateUserRequest.class), eq("admin")))
                .thenReturn(dto);

        String body = """
    {
        "email":"up@example.com",
        "role":"USER",
        "active":true
    }
    """;

        mockMvc.perform(put("/api/admin/users/50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(50))
                .andExpect(jsonPath("$.email").value("up@example.com"));
    }

    @Test
    void deleteUser_returns204() throws Exception {

        doNothing().when(adminService).deactivateUser(eq(77L), eq("admin"));

        mockMvc.perform(delete("/api/admin/users/77"))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivateUser_returnsActivatedUser() throws Exception {

        AdminUserDto dto = new AdminUserDto(
                33L, "react", "r@example.com", "USER",
                true, "PAT-33", "Re", "Act",
                LocalDate.of(1995, 7, 7),
                "7776665555", Instant.now()
        );

        when(adminService.reactivateUser(eq(33L), eq("system")))
                .thenReturn(dto);

        mockMvc.perform(put("/api/admin/users/33/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listAuditLogs_returnsLogList() throws Exception {

        AuditLogDto log = new AuditLogDto(
                1L,
                Instant.now(),
                "admin",
                "LOGIN",
                "User logged in",
                "127.0.0.1"
        );

        when(adminService.listAuditLogs(5)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/audit-logs?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("LOGIN"));
    }

    @Test
    void auditSummary_returnsSummaryMap() throws Exception {

        when(adminService.auditSummary())
                .thenReturn(Map.of("LOGIN", 10L, "DELETE", 2L));

        mockMvc.perform(get("/api/admin/audit-logs/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.LOGIN").value(10))
                .andExpect(jsonPath("$.DELETE").value(2));
    }

    @Test
    void recentAuditLogs_returnsRecent() throws Exception {

        AuditLogDto log = new AuditLogDto(
                10L, Instant.now(), "admin", "UPDATE", "Changed data", "10.0.0.1"
        );

        when(adminService.listAuditLogs(2)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/admin/audit-logs/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("UPDATE"));
    }


}
