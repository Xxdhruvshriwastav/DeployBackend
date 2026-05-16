package com.hireconnect.application.messaging;

import com.hireconnect.application.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Replaces the old RestTemplate-based NotificationClient.
 * Sends fire-and-forget notification messages to notification-service via RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmail(String toEmail, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", toEmail);
            payload.put("type", "EMAIL");
            payload.put("message", message);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    payload
            );
            log.info("[MQ] Email notification queued for: {}", toEmail);
        } catch (Exception e) {
            log.error("[MQ] Failed to queue email notification for {}: {}", toEmail, e.getMessage());
        }
    }
}
