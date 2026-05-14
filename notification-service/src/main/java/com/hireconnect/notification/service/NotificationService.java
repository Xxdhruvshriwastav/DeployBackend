package com.hireconnect.notification.service;

import com.hireconnect.notification.dto.NotificationDTO;
import java.util.List;

public interface NotificationService {
    void sendNotification(NotificationDTO notificationDTO);
    void markAsRead(Long notificationId);
    void markAllRead(String userId);
    List<NotificationDTO> getByUser(String userId);
    void deleteNotification(Long notificationId);
    void sendEmailAlert(String to, String subject, String body);
    int getUnreadCount(String userId);
}
