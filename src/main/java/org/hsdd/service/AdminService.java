package org.hsdd.service;

import org.hsdd.dto.*;
import org.hsdd.value.AuditLogEntry;
import org.hsdd.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserService userService;
    private final AuditLogRepository auditRepo;

    public AdminService(UserService userService, AuditLogRepository auditRepo) {
        this.userService = userService;
        this.auditRepo = auditRepo;
    }

    public List<AdminUserDto> listUsers() {
        return userService.getAllUsers();
    }

    public AdminUserDto getUser(Long id) {
        return userService.getUser(id);
    }

    public AdminUserDto createUser(CreateUserRequest req, String actor) {
        return userService.registerUser(req, actor);
    }

    public AdminUserDto updateUser(Long id, UpdateUserRequest req, String actor) {
        return userService.updateUser(id, req, actor);
    }

    public void deactivateUser(Long id, String actor) {
        userService.deleteUser(id, actor);
    }

    public AdminUserDto reactivateUser(Long id, String actor) {
        return userService.reactivateUser(id, actor);
    }


    public List<AuditLogDto> listAuditLogs(int limit) {
        return auditRepo.findAllByOrderByEventTimeDesc().stream()
                .limit(limit)
                .map(a -> new AuditLogDto(
                        a.getId(),
                        a.getEventTime(),
                        a.getActor(),
                        a.getEventType(),
                        a.getDetails(),
                        a.getIpAddress()
                ))
                .toList();
    }

    public Map<String, Long> auditSummary() {
        return auditRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        AuditLogEntry::getEventType,
                        Collectors.counting()
                ));
    }
}
