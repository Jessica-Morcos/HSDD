package org.hsdd.controller;

import org.hsdd.dto.*;
import org.hsdd.service.AdminService;
import org.hsdd.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ComplianceController {

    private final AdminService adminService;
    private final UserRepository userRepository;   // ⭐ REQUIRED FOR activeUsers

    public ComplianceController(AdminService adminService, UserRepository userRepository) {
        this.adminService = adminService;
        this.userRepository = userRepository;      // ⭐ SAVE IT
    }

    // ---------------- USERS ----------------

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserDto> createUser(
            @RequestBody CreateUserRequest req,
            Principal principal
    ) {
        String actor = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(adminService.createUser(req, actor));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest req,
            Principal principal
    ) {
        String actor = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(adminService.updateUser(id, req, actor));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Principal principal
    ) {
        String actor = principal != null ? principal.getName() : "admin";
        adminService.deactivateUser(id, actor);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<AdminUserDto> reactivateUser(
            @PathVariable Long id,
            Principal principal) {

        String actor = principal != null ? principal.getName() : "system";
        return ResponseEntity.ok(adminService.reactivateUser(id, actor));
    }

    // ---------------- AUDIT ----------------

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> listAuditLogs(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(adminService.listAuditLogs(limit));
    }

    @GetMapping("/audit-logs/report")
    public ResponseEntity<Map<String, Long>> auditSummary() {
        return ResponseEntity.ok(adminService.auditSummary());
    }

    @GetMapping("/audit-logs/recent")
    public ResponseEntity<List<AuditLogDto>> recentAuditLogs() {
        return ResponseEntity.ok(
                adminService.listAuditLogs(2)  // last 3 logs
        );
    }




    // ---------------- SYSTEM HEALTH ----------------

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> systemHealth() {

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        double uptimePercent = Math.min(100, uptimeMs / 1000.0 / 3600.0);

        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        long used = total - free;
        double memoryPercent = (used * 100.0) / total;

        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad();
        int cpuPercent = (cpuLoad < 0) ? 0 : (int) (cpuLoad * 100);

        // ⭐ RESTORED — this was missing
        long activeUsers = userRepository.countByActiveTrue();

        Map<String, Object> result = new HashMap<>();
        result.put("uptime", String.format("%.1f%%", uptimePercent));
        result.put("cpuUsage", cpuPercent);
        result.put("memoryUsage", (int) memoryPercent);
        result.put("activeUsers", activeUsers);   // ⭐ NOW INCLUDED
        result.put("lastBackup", "2025-11-11 23:45");

        return ResponseEntity.ok(result);
    }
}
