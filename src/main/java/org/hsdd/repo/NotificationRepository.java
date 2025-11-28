package org.hsdd.repo;

import org.hsdd.value.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(Long userId);
}
