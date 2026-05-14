package com.hireconnect.notification.consumer;

import com.hireconnect.notification.config.RabbitMQConfig;
import com.hireconnect.notification.dto.NotificationDTO;
import com.hireconnect.notification.dto.NotificationMessage;
import com.hireconnect.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeMessage(NotificationMessage message) {
        System.out.println("Message received from queue: " + message);
        
        NotificationDTO dto = NotificationDTO.builder()
                .userId(message.getUserId())
                .type(message.getType())
                .message(message.getMessage())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        notificationService.sendNotification(dto); // to save in DB
    }
}
