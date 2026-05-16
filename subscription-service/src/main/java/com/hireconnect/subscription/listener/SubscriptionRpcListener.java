package com.hireconnect.subscription.listener;

import com.hireconnect.subscription.config.RabbitMQConfig;
import com.hireconnect.subscription.dto.SubscriptionRequest;
import com.hireconnect.subscription.dto.SubscriptionResponse;
import com.hireconnect.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC Listener: handles subscription creation requests from payment-service.
 * payment-service calls convertSendAndReceive → we create subscription → return result.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRpcListener {

    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = RabbitMQConfig.RPC_CREATE_SUBSCRIPTION)
    public Map<String, Object> createSubscription(Map<String, Object> payload) {
        log.info("[RPC] createSubscription received payload: {}", payload);
        Map<String, Object> response = new HashMap<>();
        try {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setUserId(Long.valueOf(payload.get("userId").toString()));
            request.setPlan((String) payload.get("plan"));
            request.setAmount(Double.valueOf(payload.get("amount").toString()));
            request.setPaymentMode((String) payload.get("paymentMode"));
            request.setTransactionId((String) payload.get("transactionId"));

            SubscriptionResponse subResponse = subscriptionService.subscribe(request);
            response.put("success", true);
            response.put("subscriptionId", subResponse.getSubscriptionId());
            response.put("status", subResponse.getStatus());
            log.info("[RPC] Subscription created: id={}", subResponse.getSubscriptionId());
        } catch (Exception e) {
            log.error("[RPC] createSubscription failed: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
