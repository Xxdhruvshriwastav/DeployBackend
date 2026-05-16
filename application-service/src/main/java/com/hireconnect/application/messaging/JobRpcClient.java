package com.hireconnect.application.messaging;

import com.hireconnect.application.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Replaces the old RestTemplate-based JobClient.
 * Uses RabbitMQ RPC (convertSendAndReceive) to fetch recruiter email from job-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobRpcClient {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Fetches the recruiter (postedBy) email for a given jobId via RabbitMQ RPC.
     *
     * @param jobId the job ID to look up
     * @return recruiter email or null if not found
     */
    @SuppressWarnings("unchecked")
    public String getRecruiterEmail(Long jobId) {
        try {
            log.info("[RPC] Requesting recruiter email for jobId={}", jobId);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_GET_RECRUITER_EMAIL, jobId
            );

            if (reply instanceof Map) {
                Map<String, Object> response = (Map<String, Object>) reply;
                String email = (String) response.get("email");
                log.info("[RPC] Received recruiter email={} for jobId={}", email, jobId);
                return email;
            }
            log.warn("[RPC] Unexpected reply type from job-service: {}",
                    reply != null ? reply.getClass().getName() : "null");
            return null;
        } catch (Exception e) {
            log.error("[RPC] Failed to get recruiter email for jobId={}: {}", jobId, e.getMessage());
            return null;
        }
    }
}
