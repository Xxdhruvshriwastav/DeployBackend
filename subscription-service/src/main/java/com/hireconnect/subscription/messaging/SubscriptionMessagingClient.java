package com.hireconnect.subscription.messaging;

import com.hireconnect.subscription.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Replaces:
 *   - AuthServiceClient (FeignClient) → RPC to auth-service
 *   - NotificationServiceClient (FeignClient) → fire-and-forget to notification-service
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionMessagingClient {

    private final RabbitTemplate rabbitTemplate;

    /**
     * RPC: Get user info (email, role) from auth-service by userId.
     * Returns Map with keys: userId, email, role, etc.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserById(int userId) {
        try {
            log.info("[RPC] getUserById userId={}", userId);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_AUTH_GET_USER, userId);
            if (reply instanceof Map) {
                return (Map<String, Object>) reply;
            }
        } catch (Exception e) {
            log.error("[RPC] getUserById failed for userId={}: {}", userId, e.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Fire-and-forget: Send notification to notification-service via RabbitMQ.
     *
     * @param toUserId  recipient (email for EMAIL type, userId string for in-app)
     * @param type      "EMAIL" or "INFO"
     * @param message   notification text
     */
    public void sendNotification(String toUserId, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", toUserId);
            payload.put("type", type);
            payload.put("message", message);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    payload
            );
            log.info("[MQ] Notification queued for userId={} type={}", toUserId, type);
        } catch (Exception e) {
            log.error("[MQ] Failed to send notification for userId={}: {}", toUserId, e.getMessage());
        }
    }
}
