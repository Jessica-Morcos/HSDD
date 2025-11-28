package org.hsdd.service;

import org.hsdd.value.Prediction;
import org.hsdd.value.Notification;

import java.util.List;

public interface NotificationService {

    void notifyLowConfidence(Prediction prediction, double threshold);

    List<Notification> getUnreadNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);
}
