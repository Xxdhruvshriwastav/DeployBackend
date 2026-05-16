package com.hireconnect.payment.messaging;

import com.hireconnect.payment.config.RabbitMQConfig;
import com.hireconnect.payment.dto.SubscriptionRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Replaces the FeignClient SubscriptionClient.
 * Uses RabbitMQ RPC (convertSendAndReceive) to call subscription-service
 * after a successful payment verification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessagingClient {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends subscription creation request to subscription-service via RabbitMQ RPC.
     * Blocks until subscription-service replies (success/failure).
     *
     * @return true if subscription was created successfully, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean createSubscription(SubscriptionRequestDto request) {
        try {
            // Build Map payload (avoids cross-service DTO class issues)
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId",        request.getUserId());
            payload.put("plan",          request.getPlan());
            payload.put("amount",        request.getAmount());
            payload.put("paymentMode",   request.getPaymentMode());
            payload.put("transactionId", request.getTransactionId());

            log.info("[RPC] Sending createSubscription request for userId={}", request.getUserId());

            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_CREATE_SUBSCRIPTION, payload);

            if (reply instanceof Map) {
                Map<String, Object> response = (Map<String, Object>) reply;
                boolean success = Boolean.TRUE.equals(response.get("success"));
                log.info("[RPC] createSubscription reply: success={}", success);
                return success;
            }

            log.warn("[RPC] Unexpected reply from subscription-service: {}",
                    reply != null ? reply.getClass().getName() : "null");
            return false;

        } catch (Exception e) {
            log.error("[RPC] createSubscription failed for userId={}: {}",
                    request.getUserId(), e.getMessage());
            return false;
        }
    }
}
