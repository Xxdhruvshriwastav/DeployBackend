package com.hireconnect.notification.service;

import com.hireconnect.notification.dto.NotificationDTO;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationDTO dto;

    @BeforeEach
    void setUp() {
        dto = NotificationDTO.builder()
                .userId("test@gmail.com")
                .type("EMAIL")
                .message("Text Message")
                .build();
    }

    @Test
        //test notification should save and send email
    void testSendNotification() {

        notificationService.sendNotification(dto);

        // verify save
        verify(notificationRepository, times(1)).save(any(Notification.class));

        //Email send and verify
        verify(emailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendNotification_shouldOnlySave_whenTypeIsInfo() {

        dto.setType("INFO");

        notificationService.sendNotification(dto);

        verify(notificationRepository, times(1)).save(any(Notification.class));

        // Email should NOT be sent
        verify(emailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void testMarkAsRead() {

        Notification notification = Notification.builder()
                .notificationId(1L)
                .isRead(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testGetUnreadCount() {

        when(notificationRepository.countByUserIdAndIsRead("test@gmail.com", false))
                .thenReturn(5L);

        int count = notificationService.getUnreadCount("test@gmail.com");

        assertEquals(5, count);
    }

    @Test
    void testDeleteNotification() {

        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteByNotificationId(1L);
    }
}