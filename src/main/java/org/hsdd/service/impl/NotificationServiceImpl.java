package org.hsdd.service.impl;

import org.hsdd.repo.PatientRepository;
import org.hsdd.value.Notification;
import org.hsdd.value.Prediction;
import org.hsdd.model.User;
import org.hsdd.repo.NotificationRepository;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;
    PatientRepository patients;
    public NotificationServiceImpl(
            NotificationRepository notifications,
            UserRepository users,
            PatientRepository patients
    ) {
        this.notifications = notifications;
        this.users = users;
        this.patients = patients;
    }


    @Override
    public void notifyLowConfidence(Prediction prediction, double threshold) {
        if (prediction.getConfidence() >= threshold) return;

        var patient = patients.findByPatientId(prediction.getPatientId())
                .orElse(null);


        String fullName = (patient != null)
                ? patient.getFirstName() + " " + patient.getLastName()
                : "Unknown Patient";

        List<User> doctors = users.findByRole("doctor");

        for (User doc : doctors) {
            Notification n = new Notification();
            n.setUser(doc);
            n.setPrediction(prediction);


            n.setMessage("Low confidence prediction for "
                    + fullName
                    + " (ID " + prediction.getPatientId() + ")");

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
