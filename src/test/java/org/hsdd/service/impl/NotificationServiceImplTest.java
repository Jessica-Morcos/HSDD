package org.hsdd.service.impl;

import org.hsdd.domain.User;
import org.hsdd.model.Notification;
import org.hsdd.model.Prediction;
import org.hsdd.repo.NotificationRepository;
import org.hsdd.repo.UserRepository;
import org.hsdd.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notifications;

    @Mock
    private UserRepository users;

    @InjectMocks
    private NotificationServiceImpl service;

    // -------------------------------------------------------------------------
    // 1. notifyLowConfidence() — creates 1 notification PER doctor
    // -------------------------------------------------------------------------
    @Test
    void notifyLowConfidence_createsNotificationsForDoctors() {
        Prediction p = new Prediction("PAT-11111", 10L, "HSDD", 0.20);
        p.setId(99L);  // required for message text

        User doctor1 = new User();
        doctor1.setId(1L);
        doctor1.setRole("doctor");

        User doctor2 = new User();
        doctor2.setId(2L);
        doctor2.setRole("doctor");

        when(users.findByRole("doctor"))
                .thenReturn(List.of(doctor1, doctor2));

        // threshold is 0.5 → our prediction (0.20) triggers notifications
        service.notifyLowConfidence(p, 0.50);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notifications, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();

        assertEquals(2, saved.size());
        assertEquals("PAT-11111", saved.get(0).getPrediction().getPatientId());
        assertTrue(saved.get(0).getMessage().contains("Low confidence prediction"));
        assertEquals(doctor1, saved.get(0).getUser());

        assertEquals(doctor2, saved.get(1).getUser());
    }

    // -------------------------------------------------------------------------
    // 2. notifyLowConfidence() — does NOTHING when confidence >= threshold
    // -------------------------------------------------------------------------
    @Test
    void notifyLowConfidence_doesNothingWhenAboveThreshold() {
        Prediction p = new Prediction("PAT-99999", 20L, "HSDD", 0.95);

        service.notifyLowConfidence(p, 0.50);

        verifyNoInteractions(users);
        verifyNoInteractions(notifications);
    }

    // -------------------------------------------------------------------------
    // 3. markAsRead() — loads notification, sets read flag, saves it
    // -------------------------------------------------------------------------
    @Test
    void markAsRead_marksNotificationTrueAndSaves() {
        Notification n = new Notification();
        n.setReadFlag(false);

        when(notifications.findById(123L))
                .thenReturn(Optional.of(n));

        service.markAsRead(123L, 1L);

        assertTrue(n.isReadFlag());
        verify(notifications).save(n);
    }
}
