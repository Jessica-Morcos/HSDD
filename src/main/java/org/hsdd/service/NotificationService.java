package org.hsdd.service;

import org.hsdd.model.Prediction;
import org.hsdd.model.Notification;

import java.util.List;

public interface NotificationService {

    void notifyLowConfidence(Prediction prediction, double threshold);

    List<Notification> getUnreadNotifications(Long userId);

    void markAsRead(Long notificationId, Long userId);
}
