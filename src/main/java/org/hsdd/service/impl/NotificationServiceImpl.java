package org.hsdd.service.impl;

import org.hsdd.model.Notification;
import org.hsdd.model.Prediction;
import org.hsdd.domain.User;
import org.hsdd.repo.NotificationRepository;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationServiceImpl(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    // Send alerts for low-confidence predictions
    @Override
    public void notifyLowConfidence(Prediction prediction, double threshold) {
        if (prediction.getConfidence() >= threshold) return;

        List<User> doctors = users.findByRole("doctor");

        for (User doc : doctors) {
            Notification n = new Notification();
            n.setUser(doc);
            n.setPrediction(prediction);
            n.setMessage("Low confidence prediction for patient "
                    + prediction.getPatientId());

            notifications.save(n);
        }
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notifications.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notifications.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));



        n.setReadFlag(true);
        notifications.save(n);
    }
}
