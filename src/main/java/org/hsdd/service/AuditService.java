package org.hsdd.service;

import jakarta.servlet.http.HttpServletRequest;
import org.hsdd.value.AuditLogEntry;
import org.hsdd.repo.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    private final HttpServletRequest request;

    public AuditService(AuditLogRepository repo, HttpServletRequest request) {
        this.repo = repo; this.request = request;
    }

    public void log(String actor, String type, String details) {
        AuditLogEntry a = new AuditLogEntry();
        a.setActor(actor);
        a.setEventType(type);
        a.setDetails(details);
        a.setIpAddress(request.getRemoteAddr());
        repo.save(a);
    }
}
