package com.hireconnect.notification.service;

import com.hireconnect.notification.dto.NotificationDTO;
import com.hireconnect.notification.entity.Notification;
import com.hireconnect.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifRepo;
    private final JavaMailSender emailSender;   // BuiltIn Interface in SpringBoot Application

    @Override
    public void sendNotification(NotificationDTO dto) {
        Notification notification = convertToEntity(dto);
        notification.setRead(false); // Set New notification = unread
        notifRepo.save(notification);
        
        // Optionally trigger email if type warrants it
        if ("EMAIL".equalsIgnoreCase(notification.getType()) || "URGENT".equalsIgnoreCase(notification.getType())) {  // If type is email and urgent then send email
            sendEmailAlert(notification.getUserId(), "HireConnect Alert", notification.getMessage());
        }
    }

    @Override
    public void markAsRead(Long notificationId) {
        notifRepo.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notifRepo.save(notification);
        });
    }

    @Override
    @Transactional
    public void markAllRead(String userId) {
        List<Notification> unreadList = notifRepo.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
        for (Notification n : unreadList) {
            n.setRead(true);
        }
        notifRepo.saveAll(unreadList);
    }

    @Override
    public List<NotificationDTO> getByUser(String userId) {
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notifRepo.deleteByNotificationId(notificationId);
    }

    @Override
    public void sendEmailAlert(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("rudrashriwastav99393@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            emailSender.send(message);
            System.out.println("Email sent successfully to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ". Please ensure SMTP is configured. Error: " + e.getMessage());
        }
    }

    @Override
    public int getUnreadCount(String userId) {
        return (int) notifRepo.countByUserIdAndIsRead(userId, false);
    }

    private NotificationDTO convertToDTO(Notification entity) {
        return NotificationDTO.builder()
                .notificationId(entity.getNotificationId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Notification convertToEntity(NotificationDTO dto) {
        return Notification.builder()
                .notificationId(dto.getNotificationId())
                .userId(dto.getUserId())
                .type(dto.getType())
                .message(dto.getMessage())
                .isRead(dto.isRead())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
