package org.hsdd.repo;

import org.hsdd.value.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findAllByOrderByEventTimeDesc();
}
