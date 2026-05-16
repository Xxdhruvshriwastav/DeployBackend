package com.hireconnect.auth.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.auth.config.RabbitMQConfig;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRpcListener {

    private final AuthService authService;

    /**
     * RPC Listener: returns user info (as Map) for the given userId.
     * Returns a Map to avoid cross-service class deserialization issues.
     */
    @RabbitListener(queues = RabbitMQConfig.AUTH_GET_USER_BY_ID_QUEUE)
    public Map<String, Object> getUserById(Integer userId) {
        log.info("[RPC] Received getUserById request for userId={}", userId);
        try {
            UserCredential user = authService.getUserById(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getUserId());
            result.put("email", user.getEmail());
            result.put("role", user.getRole());
            result.put("provider", user.getProvider());
            result.put("isActive", user.isActive());
            log.info("[RPC] Returning user info for userId={}", userId);
            return result;
        } catch (Exception e) {
            log.error("[RPC] Error fetching user for userId={}: {}", userId, e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}
