package com.hireconnect.analytics.messaging;

import com.hireconnect.analytics.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces all 3 Feign clients (JobClient, ApplicationClient, AuthClient).
 * All calls are synchronous RPC over RabbitMQ using convertSendAndReceive.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsRpcClient {

    private final RabbitTemplate rabbitTemplate;

    // ─── AUTH-SERVICE calls ────────────────────────────────────────────────────

    /**
     * Get user info (email, role, etc.) by userId from auth-service.
     * Returns Map with keys: userId, email, role, provider, isActive.
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

    // ─── JOB-SERVICE calls ─────────────────────────────────────────────────────

    /**
     * Get all jobs posted by a recruiter (by email) from job-service.
     * Returns List of Maps each containing jobId, title, postedBy, status, etc.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getJobsByRecruiter(String email) {
        try {
            log.info("[RPC] getJobsByRecruiter email={}", email);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_JOB_BY_RECRUITER, email);
            if (reply instanceof List) {
                return (List<Map<String, Object>>) reply;
            }
        } catch (Exception e) {
            log.error("[RPC] getJobsByRecruiter failed for email={}: {}", email, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Get total job count from job-service.
     */
    public Long countJobs() {
        try {
            log.info("[RPC] countJobs");
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_JOB_COUNT, "count");
            if (reply instanceof Long)   return (Long) reply;
            if (reply instanceof Integer) return ((Integer) reply).longValue();
            if (reply instanceof Number)  return ((Number) reply).longValue();
        } catch (Exception e) {
            log.error("[RPC] countJobs failed: {}", e.getMessage());
        }
        return 0L;
    }

    // ─── APPLICATION-SERVICE calls ─────────────────────────────────────────────

    /** Total applications across the platform */
    public Long countAllApplications() {
        try {
            log.info("[RPC] countAllApplications");
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_APP_COUNT_ALL, "count");
            return toLong(reply);
        } catch (Exception e) {
            log.error("[RPC] countAllApplications failed: {}", e.getMessage());
        }
        return 0L;
    }

    /** Applications filtered by status */
    public Long countByStatus(String status) {
        try {
            log.info("[RPC] countByStatus status={}", status);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_APP_COUNT_STATUS, status);
            return toLong(reply);
        } catch (Exception e) {
            log.error("[RPC] countByStatus failed status={}: {}", status, e.getMessage());
        }
        return 0L;
    }

    /** Applications for a list of job IDs */
    public Long countByJobs(List<Long> jobIds) {
        try {
            log.info("[RPC] countByJobs count={}", jobIds.size());
            Map<String, Object> payload = new HashMap<>();
            payload.put("jobIds", jobIds);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_APP_COUNT_JOBS, payload);
            return toLong(reply);
        } catch (Exception e) {
            log.error("[RPC] countByJobs failed: {}", e.getMessage());
        }
        return 0L;
    }

    /** Applications for a list of job IDs filtered by status */
    public Long countByJobsAndStatus(List<Long> jobIds, String status) {
        try {
            log.info("[RPC] countByJobsAndStatus status={} jobs={}", status, jobIds.size());
            Map<String, Object> payload = new HashMap<>();
            payload.put("jobIds", jobIds);
            payload.put("status", status);
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.RPC_APP_COUNT_JOBS_ST, payload);
            return toLong(reply);
        } catch (Exception e) {
            log.error("[RPC] countByJobsAndStatus failed: {}", e.getMessage());
        }
        return 0L;
    }

    // ─── Helper ────────────────────────────────────────────────────────────────
    private Long toLong(Object value) {
        if (value == null)         return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number)  return ((Number) value).longValue();
        return 0L;
    }
}
