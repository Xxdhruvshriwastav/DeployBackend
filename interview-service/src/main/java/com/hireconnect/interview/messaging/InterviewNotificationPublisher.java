package com.hireconnect.interview.messaging;

import com.hireconnect.interview.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Sends interview-related email notifications to notification-service
 * via RabbitMQ fire-and-forget (convertAndSend).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * @param toEmail   recipient email address
     * @param type      "EMAIL" for actual email, "INFO" for in-app only
     * @param message   notification body
     */
    public void notify(String toEmail, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", toEmail);
            payload.put("type", type);
            payload.put("message", message);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    payload
            );
            log.info("[MQ] Interview notification queued for {} (type={})", toEmail, type);
        } catch (Exception e) {
            log.error("[MQ] Failed to queue interview notification for {}: {}", toEmail, e.getMessage());
        }
    }
}
